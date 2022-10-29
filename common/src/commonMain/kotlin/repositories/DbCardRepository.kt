package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.*

/**
 * Database repository to work with cards.
 */
interface DbCardRepository {

    /**
     * Gets card by id.
     */
    fun getCard(id: CardId): CardDbResponse

    /**
     * Gets all cards by dictionaryId.
     */
    fun getAllCards(id: DictionaryId): CardsDbResponse

    /**
     * Searches cards by filter.
     */
    fun searchCard(filter: CardFilter): CardsDbResponse

    /**
     * Creates card.
     */
    fun createCard(card: CardEntity): CardDbResponse

    /**
     * Updates.
     */
    fun updateCard(card: CardEntity): CardDbResponse

    /**
     * Updates cards details.
     */
    fun learnCards(learn: List<CardLearn>): CardsDbResponse

    /**
     * Resets status.
     */
    fun resetCard(id: CardId): CardDbResponse

    /**
     * Deletes card by id.
     */
    fun deleteCard(id: CardId): DeleteCardDbResponse
}

data class CardsDbResponse(
    val cards: List<CardEntity>,
    val sourceLanguageId: LangId = LangId.NONE,
    val errors: List<AppError> = emptyList(),
) {
    companion object {
        val EMPTY = CardsDbResponse(cards = emptyList(), errors = emptyList())
    }
}

data class CardDbResponse(
    val card: CardEntity,
    val errors: List<AppError> = emptyList(),
) {
    companion object {
        val EMPTY = CardDbResponse(card = CardEntity.EMPTY)
    }
}

data class DeleteCardDbResponse(val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = DeleteCardDbResponse(errors = emptyList())
    }
}