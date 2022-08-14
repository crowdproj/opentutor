package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

/**
 * Database repository to work with cards.
 */
interface DbCardRepository {
    /**
     * Gets all cards by dictionaryId.
     */
    fun getAllCards(id: DictionaryId): CardEntitiesDbResponse

    /**
     * Creates card.
     */
    fun createCard(card: CardEntity): CardEntityDbResponse
}

data class CardEntitiesDbResponse(val cards: List<CardEntity>, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = CardEntitiesDbResponse(cards = emptyList(), errors = emptyList())
    }
}

data class CardEntityDbResponse(val card: CardEntity, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = CardEntityDbResponse(card = CardEntity.EMPTY)
    }
}