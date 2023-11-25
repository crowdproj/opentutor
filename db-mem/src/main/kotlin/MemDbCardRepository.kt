package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.dbError
import com.gitlab.sszuev.flashcards.common.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.common.forbiddenEntityDbError
import com.gitlab.sszuev.flashcards.common.noCardFoundDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.common.validateCardLearns
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse
import java.time.LocalDateTime
import kotlin.random.Random

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbCardRepository {
    private val database = MemDatabase.get(dbConfig.dataLocation)

    override fun getCard(userId: AppUserId, cardId: CardId): CardDbResponse {
        val card =
            database.findCardById(cardId.asLong()) ?: return CardDbResponse(noCardFoundDbError("getCard", cardId))
        val errors = mutableListOf<AppError>()
        checkDictionaryUser("getCard", userId, card.dictionaryId.asDictionaryId(), cardId, errors)
        if (errors.isNotEmpty()) {
            return CardDbResponse(errors = errors)
        }
        return CardDbResponse(card = card.toCardEntity())
    }

    override fun getAllCards(userId: AppUserId, dictionaryId: DictionaryId): CardsDbResponse {
        val id = dictionaryId.asLong()
        val errors = mutableListOf<AppError>()
        val dictionary = checkDictionaryUser("getAllCards", userId, dictionaryId, dictionaryId, errors)
        if (errors.isNotEmpty() || dictionary == null) {
            return CardsDbResponse(errors = errors)
        }
        val cards = database.findCardsByDictionaryId(id).map { it.toCardEntity() }.toList()
        val dictionaries = listOf(dictionary.toDictionaryEntity())
        return CardsDbResponse(
            cards = cards,
            dictionaries = dictionaries,
            errors = emptyList()
        )
    }

    override fun searchCard(userId: AppUserId, filter: CardFilter): CardsDbResponse {
        val ids = filter.dictionaryIds.map { it.asLong() }
        val dictionariesFromDb = database.findDictionariesByIds(ids).sortedBy { it.id }.toSet()
        if (dictionariesFromDb.isEmpty()) {
            return CardsDbResponse()
        }
        val forbiddenIds =
            dictionariesFromDb.filter { it.userId != userId.asLong() }.map { checkNotNull(it.id) }.toSet()
        val errors = forbiddenIds.map { forbiddenEntityDbError("searchCards", it.asDictionaryId(), userId) }
        if (errors.isNotEmpty()) {
            return CardsDbResponse(cards = emptyList(), dictionaries = emptyList(), errors = errors)
        }
        val dictionaries = dictionariesFromDb.filterNot { it.id in forbiddenIds }.map { it.toDictionaryEntity() }
        var cardsFromDb = database.findCardsByDictionaryIds(ids)
        if (!filter.withUnknown) {
            cardsFromDb = cardsFromDb.filter { sysConfig.status(it.answered) != DocumentCardStatus.LEARNED }
        }
        if (filter.random) {
            cardsFromDb = cardsFromDb.shuffled(Random.Default)
        }
        val cards = cardsFromDb.take(filter.length).map { it.toCardEntity() }.toList()
        return CardsDbResponse(cards = cards, dictionaries = dictionaries)
    }

    override fun createCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse {
        validateCardEntityForCreate(cardEntity)
        val errors = mutableListOf<AppError>()
        checkDictionaryUser("createCard", userId, cardEntity.dictionaryId, cardEntity.dictionaryId, errors)
        if (errors.isNotEmpty()) {
            return CardDbResponse(errors = errors)
        }
        val timestamp = systemNow()
        return CardDbResponse(
            card = database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
        )
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

    override fun learnCards(userId: AppUserId, cardLearns: List<CardLearn>): CardsDbResponse {
        validateCardLearns(cardLearns)
        val timestamp = systemNow()
        val errors = mutableListOf<AppError>()
        val cards = cardLearns.mapNotNull { learnCard(it, userId, errors, timestamp) }
        if (errors.isNotEmpty()) {
            return CardsDbResponse(errors = errors)
        }
        return CardsDbResponse(cards = cards.map { it.toCardEntity() }, errors = errors)
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

    private fun learnCard(
        learn: CardLearn,
        userId: AppUserId,
        errors: MutableList<AppError>,
        timestamp: LocalDateTime,
    ): MemDbCard? {
        val cardId = learn.cardId
        val card = database.findCardById(cardId.asLong())
        if (card == null) {
            errors.add(noCardFoundDbError("learnCard", cardId))
            return null
        }
        if (checkDictionaryUser("learnCard", userId, card.dictionaryId.asDictionaryId(), cardId, errors) == null) {
            return null
        }
        return database.saveCard(card.copy(details = learn.details.toMemDbCardDetails(), changedAt = timestamp))
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