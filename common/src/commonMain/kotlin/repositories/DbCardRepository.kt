package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangId

/**
 * Database repository to work with cards.
 */
interface DbCardRepository {

    /**
     * Gets card by id.
     */
    fun getCard(cardId: CardId): CardDbResponse

    /**
     * Gets all cards by dictionaryId.
     */
    fun getAllCards(dictionaryId: DictionaryId): CardsDbResponse

    /**
     * Searches cards by filter.
     */
    fun searchCard(filter: CardFilter): CardsDbResponse

    /**
     * Creates card.
     */
    fun createCard(cardEntity: CardEntity): CardDbResponse

    /**
     * Updates.
     */
    fun updateCard(cardEntity: CardEntity): CardDbResponse

    /**
     * Updates cards details.
     */
    fun learnCards(cardLearn: List<CardLearn>): CardsDbResponse

    /**
     * Resets status.
     */
    fun resetCard(cardId: CardId): CardDbResponse

    /**
     * Deletes card by id.
     */
    fun deleteCard(cardId: CardId): DeleteCardDbResponse
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