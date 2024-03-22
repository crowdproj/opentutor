package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.dbError
import com.gitlab.sszuev.flashcards.common.forbiddenEntityDbError
import com.gitlab.sszuev.flashcards.common.noCardFoundDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.systemNow
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
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class PgDbCardRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
) : DbCardRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun findCard(cardId: CardId): CardEntity? {
        require(cardId != CardId.NONE)
        return connection.execute {
            PgDbCard.findById(cardId.asLong())?.toCardEntity()
        }
    }

    override fun findCards(dictionaryId: DictionaryId): Sequence<CardEntity> {
        require(dictionaryId != DictionaryId.NONE)
        return connection.execute {
            PgDbCard.find { Cards.dictionaryId eq dictionaryId.asLong() }.map { it.toCardEntity() }.asSequence()
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

    override fun updateCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse {
        return connection.execute {
            validateCardEntityForUpdate(cardEntity)
            val timestamp = systemNow()
            val found = PgDbCard.findById(cardEntity.cardId.asRecordId()) ?: return@execute CardDbResponse(
                noCardFoundDbError("updateCard", cardEntity.cardId)
            )
            val errors = mutableListOf<AppError>()
            val foundDictionary =
                checkDictionaryUser("updateCard", userId, cardEntity.dictionaryId, cardEntity.cardId, errors)
            if (foundDictionary != null && foundDictionary.id.value != cardEntity.dictionaryId.asLong()) {
                errors.add(
                    dbError(
                        operation = "updateCard",
                        fieldName = cardEntity.cardId.asString(),
                        details = "given and found dictionary ids do not match: ${cardEntity.dictionaryId.asString()} != ${found.dictionaryId.value}"
                    )
                )
            }
            if (errors.isNotEmpty()) {
                return@execute CardDbResponse(errors = errors)
            }
            writeCardEntityToPgDbCard(from = cardEntity, to = found, timestamp = timestamp)
            return@execute CardDbResponse(card = found.toCardEntity())
        }
    }

    override fun updateCards(
        userId: AppUserId,
        cardIds: Iterable<CardId>,
        update: (CardEntity) -> CardEntity
    ): CardsDbResponse {
        return connection.execute {
            val timestamp = systemNow()
            val ids = cardIds.map { it.asLong() }
            val dbCards = PgDbCard.find { Cards.id inList ids }.associateBy { it.id.value }
            val errors = mutableListOf<AppError>()
            ids.filterNot { it in dbCards.keys }.forEach {
                errors.add(noCardFoundDbError(operation = "updateCards", id = it.asCardId()))
            }
            val dbDictionaries = mutableMapOf<Long, PgDbDictionary>()
            dbCards.forEach {
                val dictionary = dbDictionaries.computeIfAbsent(it.value.dictionaryId.value) { k ->
                    checkNotNull(PgDbDictionary.findById(k))
                }
                if (dictionary.userId.value != userId.asLong()) {
                    errors.add(forbiddenEntityDbError("updateCards", it.key.asCardId(), userId))
                }
            }
            if (errors.isNotEmpty()) {
                return@execute CardsDbResponse(errors = errors)
            }
            val cards = dbCards.values.onEach {
                val new = update(it.toCardEntity())
                writeCardEntityToPgDbCard(from = new, to = it, timestamp = timestamp)
            }.map {
                it.toCardEntity()
            }
            val dictionaries = dbDictionaries.values.map { it.toDictionaryEntity() }
            CardsDbResponse(
                cards = cards,
                dictionaries = dictionaries,
                errors = emptyList(),
            )
        }
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