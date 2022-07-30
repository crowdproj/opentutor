package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.notFoundDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.Card
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

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

    private fun <R> execute(statement: Transaction.() -> R): R {
        return transaction(connection, statement)
    }
}