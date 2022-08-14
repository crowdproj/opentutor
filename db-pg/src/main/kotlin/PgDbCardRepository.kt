package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.dbError
import com.gitlab.sszuev.flashcards.common.notFoundDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.Card
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Example
import com.gitlab.sszuev.flashcards.dbpg.dao.Translation
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

class PgDbCardRepository(config: PgDbConfig = PgDbConfig()) : DbCardRepository {
    private val connection = PgDbConnector(config).connection

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        return execute {
            val cards = Card.find {
                Cards.dictionaryId eq id.asDbId()
            }.with(Card::examples).with(Card::translations).map { it.toEntity() }

            val errors = if (cards.isEmpty())
                listOf(notFoundDbError(operation = "getAllCards", fieldName = id.asString()))
            else emptyList()
            CardEntitiesDbResponse(
                cards = cards,
                errors = errors
            )
        }
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        return execute({
            val record = Card.new {
                copyToDbEntityRecord(from = card, to = this)
            }
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
            CardEntityDbResponse(card = record.toEntity())
        }, {
            val error = if (this.message?.contains(UNKNOWN_DICTIONARY) == true) {
                notFoundDbError(operation = "createCard", fieldName = card.dictionaryId.asString())
            } else {
                dbError(operation = "createCard", fieldName = card.cardId.asString(), exception = this)
            }
            CardEntityDbResponse(card = CardEntity.EMPTY, errors = listOf(error))
        })
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