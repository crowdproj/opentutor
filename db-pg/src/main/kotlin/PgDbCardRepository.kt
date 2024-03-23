package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.asKotlin
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.detailsAsCommonCardDetailsDto
import com.gitlab.sszuev.flashcards.common.forbiddenEntityDbError
import com.gitlab.sszuev.flashcards.common.noCardFoundDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbCard
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

class PgDbCardRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
) : DbCardRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun findCardById(cardId: CardId): CardEntity? {
        require(cardId != CardId.NONE)
        return connection.execute {
            PgDbCard.findById(cardId.asLong())?.toCardEntity()
        }
    }

    override fun findCardsByDictionaryId(dictionaryId: DictionaryId): Sequence<CardEntity> {
        require(dictionaryId != DictionaryId.NONE)
        return connection.execute {
            PgDbCard.find { Cards.dictionaryId eq dictionaryId.asLong() }.map { it.toCardEntity() }.asSequence()
        }
    }

    override fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<DictionaryId>): Sequence<CardEntity> {
        return connection.execute {
            PgDbCard.find {
                Cards.dictionaryId inList
                    dictionaryIds.onEach { require(it != DictionaryId.NONE) }.map { it.asRecordId() }.toSet()
            }.map { it.toCardEntity() }.asSequence()
        }
    }

    override fun findCardsByIdIn(cardIds: Iterable<CardId>): Sequence<CardEntity> {
        return connection.execute {
            PgDbCard.find {
                Cards.id inList cardIds.onEach { require(it != CardId.NONE) }.map { it.asRecordId() }.toSet()
            }.map { it.toCardEntity() }.asSequence()
        }
    }

    override fun createCard(cardEntity: CardEntity): CardEntity {
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

    override fun updateCard(cardEntity: CardEntity): CardEntity {
        validateCardEntityForUpdate(cardEntity)
        return connection.execute {
            val found = PgDbCard.findById(cardEntity.cardId.asRecordId())
                ?: throw DbDataException("Can't find card id = ${cardEntity.cardId.asLong()}")
            if (found.dictionaryId.value != cardEntity.dictionaryId.asLong()) {
                throw DbDataException("Changing dictionary-id is not allowed; card id = ${cardEntity.cardId.asLong()}")
            }
            val timestamp = systemNow()
            writeCardEntityToPgDbCard(from = cardEntity, to = found, timestamp = timestamp)
            found.toCardEntity()
        }
    }

    override fun updateCards(cardEntities: Iterable<CardEntity>): List<CardEntity> = connection.execute {
        val res = mutableListOf<CardEntity>()
        val timestamp = systemNow()
        BatchUpdateStatement(Cards).apply {
            cardEntities.onEach {
                validateCardEntityForUpdate(it)
                addBatch(it.cardId.asRecordId())
                this[Cards.dictionaryId] = it.dictionaryId.asRecordId()
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

    override fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse {
        return connection.execute {
            val timestamp = systemNow()
            val found = PgDbCard.findById(cardId.asLong()) ?: return@execute CardDbResponse(
                noCardFoundDbError("resetCard", cardId)
            )
            val errors = mutableListOf<AppError>()
            checkDictionaryUser("resetCard", userId, found.dictionaryId.asDictionaryId(), cardId, errors)
            if (errors.isNotEmpty()) {
                return@execute CardDbResponse(errors = errors)
            }
            writeCardEntityToPgDbCard(from = found.toCardEntity().copy(answered = 0), to = found, timestamp = timestamp)
            return@execute CardDbResponse(card = found.toCardEntity())
        }
    }

    override fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse {
        return connection.execute {
            val card = PgDbCard.findById(cardId.asLong())?.toCardEntity() ?: return@execute RemoveCardDbResponse(
                noCardFoundDbError("removeCard", cardId)
            )
            val errors = mutableListOf<AppError>()
            checkDictionaryUser("removeCard", userId, card.dictionaryId, cardId, errors)
            if (errors.isNotEmpty()) {
                return@execute RemoveCardDbResponse(errors = errors)
            }
            if (Cards.deleteWhere { this.id eq cardId.asLong() } == 0) {
                return@execute RemoveCardDbResponse(noCardFoundDbError("removeCard", cardId))
            }
            RemoveCardDbResponse(card = card)
        }
    }

    @Suppress("DuplicatedCode")
    private fun checkDictionaryUser(
        operation: String,
        userId: AppUserId,
        dictionaryId: DictionaryId,
        entityId: Id,
        errors: MutableList<AppError>
    ): PgDbDictionary? {
        val dictionary = PgDbDictionary.findById(dictionaryId.asLong())
        if (dictionary == null) {
            errors.add(noDictionaryFoundDbError(operation, dictionaryId))
            return null
        }
        if (dictionary.userId.value == userId.asLong()) {
            return dictionary
        }
        errors.add(forbiddenEntityDbError(operation, entityId, userId))
        return null
    }
}