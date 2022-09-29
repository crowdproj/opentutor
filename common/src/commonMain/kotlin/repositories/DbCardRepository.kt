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
    fun getCard(id: CardId): CardEntityDbResponse

    /**
     * Gets all cards by dictionaryId.
     */
    fun getAllCards(id: DictionaryId): CardEntitiesDbResponse

    /**
     * Searches cards by filter.
     */
    fun searchCard(filter: CardFilter): CardEntitiesDbResponse

    /**
     * Creates card.
     */
    fun createCard(card: CardEntity): CardEntityDbResponse

    /**
     * Updates.
     */
    fun updateCard(card: CardEntity): CardEntityDbResponse

    /**
     * Updates cards details.
     */
    fun learnCards(learn: List<CardLearn>): CardEntitiesDbResponse

    /**
     * Resets status.
     */
    fun resetCard(id: CardId): CardEntityDbResponse

    /**
     * Deletes card by id.
     */
    fun deleteCard(id: CardId): DeleteEntityDbResponse
}

data class CardEntitiesDbResponse(
    val cards: List<CardEntity>,
    val sourceLanguage: LangId = LangId.NONE,
    val errors: List<AppError> = emptyList(),
) {
    companion object {
        val EMPTY = CardEntitiesDbResponse(cards = emptyList(), errors = emptyList())
    }
}

data class CardEntityDbResponse(
    val card: CardEntity,
    val errors: List<AppError> = emptyList(),
) {
    companion object {
        val EMPTY = CardEntityDbResponse(card = CardEntity.EMPTY)
    }
}

data class DeleteEntityDbResponse(val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = DeleteEntityDbResponse(errors = emptyList())
    }
}