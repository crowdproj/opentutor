package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteCardDbResponse
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class PgDbCardRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbCardRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun getCard(id: CardId): CardDbResponse {
        return connection.execute {
            val card = Card.findById(id.asLong())
            if (card == null) {
                CardDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError(operation = "getCard", id = id))
                )
            } else {
                CardDbResponse(card = card.toEntity())
            }
        }
    }

    override fun getAllCards(id: DictionaryId): CardsDbResponse {
        return connection.execute {
            val dictionary = Dictionary.findById(id.asLong())?.toEntity() ?: return@execute CardsDbResponse(
                cards = emptyList(),
                errors = listOf(noDictionaryFoundDbError(operation = "getAllCards", id = id))
            )
            val cards = Card.find {
                Cards.dictionaryId eq id.asLong()
            }.with(Card::examples).with(Card::translations).map { it.toEntity() }
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
            val dictionaries = Dictionary.find(Dictionaries.id inList dictionaryIds)
                .with(Dictionary::sourceLang)
                .with(Dictionary::targetLand)
                .map { it.toEntity() }
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
            val cards = Card.find {
                Cards.dictionaryId inList dictionaryIds and
                        (if (filter.withUnknown) Op.TRUE else Cards.answered.isNull() or Cards.answered.lessEq(learned))
            }.orderBy(random to SortOrder.ASC)
                .orderBy(Cards.dictionaryId to SortOrder.ASC)
                .limit(filter.length)
                .with(Card::examples)
                .with(Card::translations)
                .map { it.toEntity() }
            CardsDbResponse(cards = cards, sourceLanguageId = sourceLanguages.single().langId)
        }
    }

    override fun createCard(card: CardEntity): CardDbResponse {
        return connection.execute({
            requireNew(card)
            val record = Card.new {
                copyToDbEntityRecord(from = card, to = this)
            }
            createExamplesAndTranslations(card, record)
            CardDbResponse(card = record.toEntity())
        }, {
            val error = if (this.message?.contains(UNKNOWN_DICTIONARY) == true) {
                noDictionaryFoundDbError(operation = "createCard", card.dictionaryId)
            } else {
                dbError(operation = "createCard", fieldName = card.cardId.asString(), exception = this)
            }
            CardDbResponse(card = CardEntity.EMPTY, errors = listOf(error))
        })
    }

    override fun updateCard(card: CardEntity): CardDbResponse {
        return connection.execute({
            requireExiting(card)
            Examples.deleteWhere {
                this.cardId eq card.cardId.asLong()
            }
            Translations.deleteWhere {
                this.cardId eq card.cardId.asLong()
            }
            val record = Card.findById(card.cardId.asRecordId())
            if (record == null) {
                CardDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError("updateCard", card.cardId))
                )
            } else {
                copyToDbEntityRecord(from = card, to = record)
                createExamplesAndTranslations(card, record)
                CardDbResponse(card = record.toEntity())
            }
        }, {
            val error = if (this.message?.contains(UNKNOWN_DICTIONARY) == true) {
                noDictionaryFoundDbError(operation = "updateCard", card.dictionaryId)
            } else {
                dbError(operation = "updateCard", fieldName = card.cardId.asString(), exception = this)
            }
            CardDbResponse(card = CardEntity.EMPTY, errors = listOf(error))
        })
    }

    override fun learnCards(learn: List<CardLearn>): CardsDbResponse {
        return connection.execute {
            val cardLearns = learn.associateBy { it.cardId.asLong() }
            val records = Card.find {
                Cards.id inList cardLearns.keys
            }.associateBy { it.id.value }
            val errors = cardLearns.keys.filterNot { records.containsKey(it) }.map {
                noCardFoundDbError(operation = "getCard", id = CardId(it.toString()))
            }
            val cards = cardLearns.values.mapNotNull { learn ->
                val record = records[learn.cardId.asLong()] ?: return@mapNotNull null
                record.details = toDbRecordDetails(learn.details)
                record.toEntity()
            }
            CardsDbResponse(cards = cards, errors = errors)
        }
    }

    override fun resetCard(id: CardId): CardDbResponse {
        return connection.execute {
            val card = Card.findById(id.asLong())
            if (card == null) {
                CardDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError(operation = "resetCard", id = id))
                )
            } else {
                card.answered = 0
                CardDbResponse(card = card.toEntity())
            }
        }
    }

    override fun deleteCard(id: CardId): DeleteCardDbResponse {
        return connection.execute {
            Examples.deleteWhere {
                this.cardId eq id.asLong()
            }
            Translations.deleteWhere {
                this.cardId eq id.asLong()
            }
            val res = Cards.deleteWhere {
                this.id eq id.asLong()
            }
            DeleteCardDbResponse(
                if (res == 0) listOf(noCardFoundDbError(operation = "deleteCard", id = id)) else emptyList()
            )
        }
    }

    private fun createExamplesAndTranslations(card: CardEntity, record: Card) {
        // TODO: separated tables for examples and translations was a bad idea - need to fix it with json-column
        card.examples.forEach {
            Example.new {
                copyToDbExampleRecord(txt = it, card = record, to = this)
            }
        }
        card.translations.forEach {
            Translation.new {
                copyToDbTranslationRecord(txt = it, card = record, to = this)
            }
        }
    }
}