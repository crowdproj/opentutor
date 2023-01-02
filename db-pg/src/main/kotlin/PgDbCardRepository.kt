package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.dbError
import com.gitlab.sszuev.flashcards.common.noCardFoundDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.common.wrongDictionaryLanguageFamilies
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.DbPgCard
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteCardDbResponse
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import java.time.LocalDateTime

class PgDbCardRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbCardRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun getCard(cardId: CardId): CardDbResponse {
        return connection.execute {
            val card = DbPgCard.findById(cardId.asLong())
            if (card == null) {
                CardDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError(operation = "getCard", id = cardId))
                )
            } else {
                CardDbResponse(card = card.toCardEntity())
            }
        }
    }

    override fun getAllCards(dictionaryId: DictionaryId): CardsDbResponse {
        return connection.execute {
            val dictionary =
                PgDbDictionary.findById(dictionaryId.asLong())?.toDictionaryEntity() ?: return@execute CardsDbResponse(
                    cards = emptyList(),
                    errors = listOf(noDictionaryFoundDbError(operation = "getAllCards", id = dictionaryId))
                )
            val cards = DbPgCard.find { Cards.dictionaryId eq dictionaryId.asLong() }.map { it.toCardEntity() }
            CardsDbResponse(
                cards = cards,
                sourceLanguageId = dictionary.sourceLang.langId,
                errors = emptyList()
            )
        }
    }

    override fun searchCard(filter: CardFilter): CardsDbResponse {
        val dictionaryIds = filter.dictionaryIds.map { it.asLong() }
        val learned = sysConfig.numberOfRightAnswers
        val random = CustomFunction<Double>("random", DoubleColumnType())
        return connection.execute {
            val dictionaries = PgDbDictionary.find(Dictionaries.id inList dictionaryIds)
                .map { it.toDictionaryEntity() }
            val sourceLanguages = dictionaries.map { it.sourceLang }.toSet()
            val targetLanguages = dictionaries.map { it.targetLang }.toSet()
            if (sourceLanguages.size != 1 || targetLanguages.size != 1) {
                return@execute CardsDbResponse(
                    cards = emptyList(),
                    errors = listOf(
                        wrongDictionaryLanguageFamilies(
                            operation = "searchCard",
                            dictionaryIds = filter.dictionaryIds,
                        )
                    )
                )
            }
            val cards = DbPgCard.find {
                Cards.dictionaryId inList dictionaryIds and
                    (if (filter.withUnknown) Op.TRUE else Cards.answered.isNull() or Cards.answered.lessEq(learned))
            }.orderBy(random to SortOrder.ASC)
                .orderBy(Cards.dictionaryId to SortOrder.ASC)
                .limit(filter.length)
                .map { it.toCardEntity() }
            CardsDbResponse(cards = cards, sourceLanguageId = sourceLanguages.single().langId)
        }
    }

    override fun createCard(cardEntity: CardEntity): CardDbResponse {
        return connection.execute({
            validateCardEntityForCreate(cardEntity)
            val timestamp = LocalDateTime.now()
            val record = DbPgCard.new {
                writeCardEntityToPgDbCard(from = cardEntity, to = this, timestamp = timestamp)
            }
            CardDbResponse(card = record.toCardEntity())
        }, {
            val error = if (this.message?.contains(UNKNOWN_DICTIONARY) == true) {
                noDictionaryFoundDbError(operation = "createCard", cardEntity.dictionaryId)
            } else {
                dbError(operation = "createCard", fieldName = cardEntity.cardId.asString(), exception = this)
            }
            CardDbResponse(card = CardEntity.EMPTY, errors = listOf(error))
        })
    }

    override fun updateCard(cardEntity: CardEntity): CardDbResponse {
        return connection.execute({
            validateCardEntityForUpdate(cardEntity)
            val timestamp = LocalDateTime.now()
            val record = DbPgCard.findById(cardEntity.cardId.asRecordId())
            if (record == null) {
                CardDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError("updateCard", cardEntity.cardId))
                )
            } else {
                writeCardEntityToPgDbCard(from = cardEntity, to = record, timestamp = timestamp)
                CardDbResponse(card = record.toCardEntity())
            }
        }, {
            val error = if (this.message?.contains(UNKNOWN_DICTIONARY) == true) {
                noDictionaryFoundDbError(operation = "updateCard", cardEntity.dictionaryId)
            } else {
                dbError(operation = "updateCard", fieldName = cardEntity.cardId.asString(), exception = this)
            }
            CardDbResponse(card = CardEntity.EMPTY, errors = listOf(error))
        })
    }

    override fun learnCards(cardLearn: List<CardLearn>): CardsDbResponse {
        return connection.execute {
            val cardLearns = cardLearn.associateBy { it.cardId.asLong() }
            val records = DbPgCard.find { Cards.id inList cardLearns.keys }.associateBy { it.id.value }
            val errors = cardLearns.keys.filterNot { records.containsKey(it) }.map {
                noCardFoundDbError(operation = "getCard", id = CardId(it.toString()))
            }
            val cards = cardLearns.values.mapNotNull { learn ->
                val record = records[learn.cardId.asLong()] ?: return@mapNotNull null
                record.details = learn.details.toPgDbCardDetailsJson()
                record.toCardEntity()
            }
            CardsDbResponse(cards = cards, errors = errors)
        }
    }

    override fun resetCard(cardId: CardId): CardDbResponse {
        return connection.execute {
            val record = DbPgCard.findById(cardId.asLong())
            if (record == null) {
                CardDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError(operation = "resetCard", id = cardId))
                )
            } else {
                record.answered = 0
                CardDbResponse(card = record.toCardEntity())
            }
        }
    }

    override fun deleteCard(cardId: CardId): DeleteCardDbResponse {
        return connection.execute {
            val res = Cards.deleteWhere {
                this.id eq cardId.asLong()
            }
            DeleteCardDbResponse(
                if (res == 0) listOf(noCardFoundDbError(operation = "deleteCard", id = cardId)) else emptyList()
            )
        }
    }
}