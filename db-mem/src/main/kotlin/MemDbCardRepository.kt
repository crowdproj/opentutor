package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.dbError
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
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbCardRepository {
    private val database = MemDatabase.get(dbConfig.dataLocation)

    override fun findCard(cardId: CardId): CardEntity? {
        require(cardId != CardId.NONE)
        return database.findCardById(cardId.asLong())?.toCardEntity()
    }

    override fun findCards(dictionaryId: DictionaryId): Sequence<CardEntity> {
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

    override fun updateCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse {
        validateCardEntityForUpdate(cardEntity)
        val timestamp = systemNow()
        val found = database.findCardById(cardEntity.cardId.asLong()) ?: return CardDbResponse(
            noCardFoundDbError("updateCard", cardEntity.cardId)
        )
        val errors = mutableListOf<AppError>()
        val foundDictionary =
            checkDictionaryUser("updateCard", userId, cardEntity.dictionaryId, cardEntity.cardId, errors)
        if (foundDictionary != null && foundDictionary.id != cardEntity.dictionaryId.asLong()) {
            errors.add(
                dbError(
                    operation = "updateCard",
                    fieldName = cardEntity.cardId.asString(),
                    details = "given and found dictionary ids do not match: ${cardEntity.dictionaryId.asString()} != ${found.dictionaryId}"
                )
            )
        }
        if (errors.isNotEmpty()) {
            return CardDbResponse(errors = errors)
        }
        return CardDbResponse(
            card = database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
        )
    }

    override fun updateCards(
        userId: AppUserId,
        cardIds: Iterable<CardId>,
        update: (CardEntity) -> CardEntity
    ): CardsDbResponse {
        val timestamp = systemNow()
        val ids = cardIds.map { it.asLong() }
        val dbCards = database.findCardsById(ids).associateBy { checkNotNull(it.id) }
        val errors = mutableListOf<AppError>()
        val dbDictionaries = mutableMapOf<Long, MemDbDictionary>()
        dbCards.forEach {
            val dictionary = dbDictionaries.computeIfAbsent(checkNotNull(it.value.dictionaryId)) { k ->
                checkNotNull(database.findDictionaryById(k))
            }
            if (dictionary.userId != userId.asLong()) {
                errors.add(forbiddenEntityDbError("updateCards", it.key.asCardId(), userId))
            }
        }
        if (errors.isNotEmpty()) {
            return CardsDbResponse(errors = errors)
        }
        val cards = dbCards.values.map {
            val dbCard = update(it.toCardEntity()).toMemDbCard().copy(changedAt = timestamp)
            database.saveCard(dbCard).toCardEntity()
        }
        val dictionaries = dbDictionaries.values.map { it.toDictionaryEntity() }
        return CardsDbResponse(
            cards = cards,
            dictionaries = dictionaries,
            errors = emptyList(),
        )
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