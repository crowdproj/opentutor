package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.CardCorProcessor

class CardService {
    private val processor = CardCorProcessor()

    /**
     * Gets a card-resource (byte array from TTS service).
     */
    suspend fun getResource(context: CardContext): CardContext = context.exec()

    /**
     * Creates a card-entity.
     */
    suspend fun createCard(context: CardContext): CardContext = context.exec()

    /**
     * Updates a card-entity.
     */
    suspend fun updateCard(context: CardContext): CardContext = context.exec()

    /**
     * Gets a deck of cards by parameters.
     */
    suspend fun searchCards(context: CardContext): CardContext = context.exec()

    /**
     * Gets all cards by dictionary id.
     */
    suspend fun getAllCards(context: CardContext): CardContext = context.exec()

    /**
     * Gets card by id.
     */
    suspend fun getCard(context: CardContext): CardContext = context.exec()

    /**
     * Updates card learning status (process learn info).
     */
    suspend fun learnCard(context: CardContext): CardContext = context.exec()

    /**
     * Resets the card status (any -> unknown).
     */
    suspend fun resetCard(context: CardContext): CardContext = context.exec()

    /**
     * Deletes the card.
     */
    suspend fun deleteCard(context: CardContext): CardContext = context.exec()

    private suspend fun CardContext.exec(): CardContext {
        processor.execute(this)
        return this
    }
}