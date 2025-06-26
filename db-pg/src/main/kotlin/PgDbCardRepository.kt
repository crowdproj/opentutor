package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.asKotlin
import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbCard
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.systemNow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

class PgDbCardRepository(
    dbConfig: PgDbConfig = PgDbConfig.DEFAULT,
) : DbCardRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbStandardConnector.connector(dbConfig).database
    }

    // enforce connection
    fun connect() {
        connection
    }

    override fun findCardById(cardId: String): DbCard? {
        require(cardId.isNotBlank())
        return connection.execute {
            PgDbCard.findById(cardId.toLong())?.toCardEntity()
        }
    }

    override fun findCardsByDictionaryId(dictionaryId: String): Sequence<DbCard> {
        require(dictionaryId.isNotBlank())
        return connection.execute {
            PgDbCard.find { Cards.dictionaryId eq dictionaryId.toLong() }.map { it.toCardEntity() }.asSequence()
        }
    }

    override fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<String>): Sequence<DbCard> {
        return connection.execute {
            PgDbCard.find {
                Cards.dictionaryId inList
                    dictionaryIds.onEach { require(it.isNotBlank()) }.map { it.toDictionariesId() }.toSet()
            }.map { it.toCardEntity() }.asSequence()
        }
    }

    override fun findCardsByIdIn(cardIds: Iterable<String>): Sequence<DbCard> {
        return connection.execute {
            PgDbCard.find {
                Cards.id inList cardIds.onEach { require(it.isNotBlank()) }.map { it.toCardsId() }.toSet()
            }.map { it.toCardEntity() }.asSequence()
        }
    }

    override fun createCard(cardEntity: DbCard): DbCard {
        validateCardEntityForCreate(cardEntity)
        return connection.execute {
            val timestamp = systemNow()
            try {
                PgDbCard.new {
                    writeCardEntityToPgDbCard(from = cardEntity, to = this, timestamp = timestamp)
                }.toCardEntity()
            } catch (ex: Exception) {
                throw DbDataException("Can't create card $cardEntity", ex)
            }
        }
    }

    override fun updateCard(cardEntity: DbCard): DbCard {
        validateCardEntityForUpdate(cardEntity)
        return connection.execute {
            val found = PgDbCard.findById(cardEntity.cardId.toCardsId())
                ?: throw DbDataException("Can't find card id = ${cardEntity.cardId}")
            if (found.dictionaryId.value != cardEntity.dictionaryId.toLong()) {
                throw DbDataException("Changing dictionary-id is not allowed; card id = ${cardEntity.cardId}")
            }
            val timestamp = systemNow()
            writeCardEntityToPgDbCard(from = cardEntity, to = found, timestamp = timestamp)
            found.toCardEntity()
        }
    }

    override fun updateCards(cardEntities: Iterable<DbCard>): List<DbCard> = connection.execute {
        val res = mutableListOf<DbCard>()
        val timestamp = systemNow()
        BatchUpdateStatement(Cards).apply {
            cardEntities.onEach {
                validateCardEntityForUpdate(it)
                addBatch(it.cardId.toCardsId())
                this[Cards.dictionaryId] = it.dictionaryId.toDictionariesId()
                this[Cards.words] = it.toPgDbCardWordsJson()
                this[Cards.answered] = it.answered
                this[Cards.details] = it.detailsAsCommonCardDetailsDto().toJsonString()
                this[Cards.changedAt] = timestamp
            }.forEach {
                res.add(it.copy(changedAt = timestamp.asKotlin()))
            }
            execute(TransactionManager.current())
        }
        res
    }

    override fun deleteCard(cardId: String): DbCard {
        return connection.execute {
            val timestamp = systemNow()
            val card = PgDbCard.findById(cardId.toCardsId())?.toCardEntity()
                ?: throw DbDataException("Can't find card, id = $cardId")
            if (Cards.deleteWhere { this.id eq cardId.toLong() } == 0) {
                throw DbDataException("Can't delete card, id = $cardId")
            }
            card.copy(changedAt = timestamp.asKotlin())
        }
    }

    override fun countCardsByDictionaryId(dictionaryIds: Iterable<String>): Map<String, Long> {
        return connection.execute {
            Cards.select(Cards.dictionaryId, Cards.dictionaryId.count())
                .where {
                    Cards.dictionaryId inList
                        dictionaryIds.onEach { require(it.isNotBlank()) }.map { it.toDictionariesId() }.toSet()
                }
                .groupBy(Cards.dictionaryId)
                .associate {
                    it[Cards.dictionaryId].value.toString() to it[Cards.dictionaryId.count()]
                }
        }
    }

    override fun countCardsByDictionaryIdAndAnswered(
        dictionaryIds: Iterable<String>,
        greaterOrEqual: Int
    ): Map<String, Long> {
        return connection.execute {
            Cards.select(Cards.dictionaryId, Cards.dictionaryId.count())
                .where {
                    (Cards.dictionaryId inList
                        dictionaryIds.onEach { require(it.isNotBlank()) }.map { it.toDictionariesId() }.toSet()) and
                        (Cards.answered greaterEq greaterOrEqual)
                }
                .groupBy(Cards.dictionaryId)
                .associate {
                    it[Cards.dictionaryId].value.toString() to it[Cards.dictionaryId.count()]
                }
        }
    }
}