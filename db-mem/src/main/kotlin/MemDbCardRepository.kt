package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.forbiddenEntityDbError
import com.gitlab.sszuev.flashcards.common.noCardFoundDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
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

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbCardRepository {
    private val database = MemDatabase.get(dbConfig.dataLocation)

    override fun findCardById(cardId: CardId): CardEntity? {
        require(cardId != CardId.NONE)
        return database.findCardById(cardId.asLong())?.toCardEntity()
    }

    override fun findCardsByDictionaryId(dictionaryId: DictionaryId): Sequence<CardEntity> {
        require(dictionaryId != DictionaryId.NONE)
        return database.findCardsByDictionaryId(dictionaryId.asLong()).map { it.toCardEntity() }
    }

    override fun createCard(cardEntity: CardEntity): CardEntity {
        validateCardEntityForCreate(cardEntity)
        val timestamp = systemNow()
        return try {
            database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
        } catch (ex: Exception) {
            throw DbDataException("Can't create card $cardEntity", ex)
        }
    }

    override fun updateCard(cardEntity: CardEntity): CardEntity {
        validateCardEntityForUpdate(cardEntity)
        val found = database.findCardById(cardEntity.cardId.asLong())
            ?: throw DbDataException("Can't find card, id = ${cardEntity.cardId.asLong()}")
        if (found.dictionaryId != cardEntity.dictionaryId.asLong()) {
            throw DbDataException("Changing dictionary-id is not allowed; card id = ${cardEntity.cardId.asLong()}")
        }
        val timestamp = systemNow()
        return database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
    }

    override fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse {
        val timestamp = systemNow()
        val card =
            database.findCardById(cardId.asLong()) ?: return CardDbResponse(noCardFoundDbError("resetCard", cardId))
        val errors = mutableListOf<AppError>()
        checkDictionaryUser("resetCard", userId, card.dictionaryId.asDictionaryId(), cardId, errors)
        if (errors.isNotEmpty()) {
            return CardDbResponse(errors = errors)
        }
        return CardDbResponse(card = database.saveCard(card.copy(answered = 0, changedAt = timestamp)).toCardEntity())
    }

    override fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse {
        val timestamp = systemNow()
        val card = database.findCardById(cardId.asLong()) ?: return RemoveCardDbResponse(
            noCardFoundDbError("removeCard", cardId)
        )
        val errors = mutableListOf<AppError>()
        checkDictionaryUser("removeCard", userId, card.dictionaryId.asDictionaryId(), cardId, errors)
        if (errors.isNotEmpty()) {
            return RemoveCardDbResponse(errors = errors)
        }
        if (!database.deleteCardById(cardId.asLong())) {
            return RemoveCardDbResponse(noCardFoundDbError("removeCard", cardId))
        }
        return RemoveCardDbResponse(card = card.copy(changedAt = timestamp).toCardEntity())
    }

    @Suppress("DuplicatedCode")
    private fun checkDictionaryUser(
        operation: String,
        userId: AppUserId,
        dictionaryId: DictionaryId,
        entityId: Id,
        errors: MutableList<AppError>
    ): MemDbDictionary? {
        val dictionary = database.findDictionaryById(dictionaryId.asLong())
        if (dictionary == null) {
            errors.add(noDictionaryFoundDbError(operation, dictionaryId))
            return null
        }

        if (dictionary.userId == userId.asLong()) {
            return dictionary
        }
        errors.add(forbiddenEntityDbError(operation, entityId, userId))
        return null
    }
}