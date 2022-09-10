package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories

interface CardService {

    /**
     * Provides access to repositories configuration.
     */
    fun repositories(): CardRepositories

    /**
     * Gets a card-resource (byte array from TTS service).
     */
    suspend fun getResource(context: CardContext): CardContext

    /**
     * Creates a card-entity.
     */
    suspend fun createCard(context: CardContext): CardContext

    /**
     * Updates a card-entity.
     */
    suspend fun updateCard(context: CardContext): CardContext

    /**
     * Gets a deck of cards by parameters.
     */
    suspend fun searchCards(context: CardContext): CardContext

    /**
     * Gets all cards by dictionary id.
     */
    suspend fun getAllCards(context: CardContext): CardContext

    /**
     * Gets card by id.
     */
    suspend fun getCard(context: CardContext): CardContext

    /**
     * Updates card learning status (process learn info).
     */
    suspend fun learnCard(context: CardContext): CardContext

    /**
     * Resets the card status (any -> unknown).
     */
    suspend fun resetCard(context: CardContext): CardContext

    /**
     * Deletes the card.
     */
    suspend fun deleteCard(context: CardContext): CardContext
}