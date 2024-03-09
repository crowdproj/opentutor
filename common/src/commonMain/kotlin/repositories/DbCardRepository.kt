package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

/**
 * Database repository to work with cards.
 */
interface DbCardRepository {

    /**
     * Gets card by id.
     */
    fun getCard(userId: AppUserId, cardId: CardId): CardDbResponse

    /**
     * Gets all cards by dictionaryId.
     */
    fun getAllCards(userId: AppUserId, dictionaryId: DictionaryId): CardsDbResponse

    /**
     * Searches cards by filter.
     */
    fun searchCard(userId: AppUserId, filter: CardFilter): CardsDbResponse

    /**
     * Creates card.
     */
    fun createCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse

    /**
     * Updates.
     */
    fun updateCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse

    /**
     * Performs bulk update.
     */
    fun updateCards(userId: AppUserId, cardIds: Iterable<CardId>, update: (CardEntity) -> CardEntity): CardsDbResponse

    /**
     * Updates cards details.
     */
    fun learnCards(userId: AppUserId, cardLearns: List<CardLearn>): CardsDbResponse

    /**
     * Resets status.
     */
    fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse

    /**
     * Deletes card by id.
     */
    fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse
}

data class CardsDbResponse(
    val cards: List<CardEntity> = emptyList(),
    val dictionaries: List<DictionaryEntity> = emptyList(),
    val errors: List<AppError> = emptyList(),
) {

    companion object {
        val EMPTY = CardsDbResponse(cards = emptyList(), dictionaries = emptyList(), errors = emptyList())
    }
}

data class CardDbResponse(
    val card: CardEntity = CardEntity.EMPTY,
    val errors: List<AppError> = emptyList(),
) {
    constructor(error: AppError) : this(errors = listOf(error))

    companion object {
        val EMPTY = CardDbResponse(card = CardEntity.EMPTY)
    }
}

data class RemoveCardDbResponse(
    val card: CardEntity = CardEntity.EMPTY,
    val errors: List<AppError> = emptyList(),
) {
    constructor(error: AppError) : this(errors = listOf(error))

    companion object {
        val EMPTY = RemoveCardDbResponse(errors = emptyList())
    }
}