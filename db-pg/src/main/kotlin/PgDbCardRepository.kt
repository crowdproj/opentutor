package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteEntityDbResponse
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

class PgDbCardRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbCardRepository {
    private val connection = PgDbConnector(dbConfig).connection

    override fun getCard(id: CardId): CardEntityDbResponse {
        return execute {
            val card = Card.findById(id.asDbId())
            if (card == null) {
                CardEntityDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError(operation = "getCard", id = id))
                )
            } else {
                CardEntityDbResponse(card = card.toEntity())
            }
        }
    }

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        return execute {
            val cards = Card.find {
                Cards.dictionaryId eq id.asDbId()
            }.with(Card::examples).with(Card::translations).map { it.toEntity() }

            val errors = if (cards.isEmpty())
                listOf(noDictionaryFoundDbError(operation = "getAllCards", id = id))
            else emptyList()
            CardEntitiesDbResponse(
                cards = cards,
                errors = errors
            )
        }
    }

    override fun searchCard(filter: CardFilter): CardEntitiesDbResponse {
        val dictionaryIds = filter.dictionaryIds.map { it.asDbId() }
        val learned = sysConfig.numberOfRightAnswers
        val random = CustomFunction<Double>("random", DoubleColumnType())
        return execute {
            val cards = Card.find {
                Cards.dictionaryId inList dictionaryIds and
                        (if (filter.withUnknown) Op.TRUE else Cards.answered.isNull() or Cards.answered.lessEq(learned))
            }.orderBy(random to SortOrder.ASC)
                .orderBy(Cards.dictionaryId to SortOrder.ASC)
                .limit(filter.length)
                .with(Card::examples)
                .with(Card::translations)
                .map { it.toEntity() }
            CardEntitiesDbResponse(cards = cards)
        }
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        return execute({
            requireNew(card)
            val record = Card.new {
                copyToDbEntityRecord(from = card, to = this)
            }
            createExamplesAndTranslations(card, record)
            CardEntityDbResponse(card = record.toEntity())
        }, {
            val error = if (this.message?.contains(UNKNOWN_DICTIONARY) == true) {
                noDictionaryFoundDbError(operation = "createCard", card.dictionaryId)
            } else {
                dbError(operation = "createCard", fieldName = card.cardId.asString(), exception = this)
            }
            CardEntityDbResponse(card = CardEntity.EMPTY, errors = listOf(error))
        })
    }

    override fun updateCard(card: CardEntity): CardEntityDbResponse {
        return execute({
            requireExiting(card)
            Examples.deleteWhere {
                Examples.cardId eq card.cardId.asDbId()
            }
            Translations.deleteWhere {
                Translations.cardId eq card.cardId.asDbId()
            }
            val record = Card.findById(card.cardId.asRecordId())
            if (record == null) {
                CardEntityDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError("updateCard", card.cardId))
                )
            } else {
                copyToDbEntityRecord(from = card, to = record)
                createExamplesAndTranslations(card, record)
                CardEntityDbResponse(card = record.toEntity())
            }
        }, {
            val error = if (this.message?.contains(UNKNOWN_DICTIONARY) == true) {
                noDictionaryFoundDbError(operation = "updateCard", card.dictionaryId)
            } else {
                dbError(operation = "updateCard", fieldName = card.cardId.asString(), exception = this)
            }
            CardEntityDbResponse(card = CardEntity.EMPTY, errors = listOf(error))
        })
    }

    override fun learnCards(learn: List<CardLearn>): CardEntitiesDbResponse {
        return execute {
            val cardLearns = learn.associateBy { it.cardId.asDbId() }
            val records = Card.find {
                Cards.id inList cardLearns.keys
            }.associateBy { it.id.value }
            val errors = cardLearns.keys.filterNot { records.containsKey(it) }.map {
                noCardFoundDbError(operation = "getCard", id = CardId(it.toString()))
            }
            val cards = cardLearns.values.mapNotNull { learn ->
                val record = records[learn.cardId.asDbId()] ?: return@mapNotNull null
                record.details = toDbRecordDetails(learn.details)
                record.toEntity()
            }
            CardEntitiesDbResponse(cards = cards, errors = errors)
        }
    }

    override fun resetCard(id: CardId): CardEntityDbResponse {
        return execute {
            val card = Card.findById(id.asDbId())
            if (card == null) {
                CardEntityDbResponse(
                    card = CardEntity.EMPTY,
                    errors = listOf(noCardFoundDbError(operation = "resetCard", id = id))
                )
            } else {
                card.answered = 0
                CardEntityDbResponse(card = card.toEntity())
            }
        }
    }

    override fun deleteCard(id: CardId): DeleteEntityDbResponse {
        return execute {
            val res = Cards.deleteWhere {
                Cards.id eq id.asDbId()
            }
            DeleteEntityDbResponse(
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

    private fun <R> execute(statement: Transaction.() -> R): R {
        return transaction(connection, statement)
    }

    private fun <R> execute(statement: Transaction.() -> R, handleError: Exception.() -> R): R {
        return try {
            transaction(connection, statement)
        } catch (ex: SQLException) {
            handleError(ex)
        }
    }
}