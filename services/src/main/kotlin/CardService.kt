package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.CardContext

interface CardService {

    /**
     * Creates a card-entity.
     */
    fun createCard(context: CardContext): CardContext

    /**
     * Updates a card-entity.
     */
    fun updateCard(context: CardContext): CardContext


    /**
     * Gets a deck of cards by parameters.
     */
    fun searchCards(context: CardContext): CardContext

    /**
     * Gets card by id.
     */
    fun getCard(context: CardContext): CardContext

    /**
     * Updates card learning status (process learn info).
     */
    fun learnCard(context: CardContext): CardContext

    /**
     * Resets the card status (any -> unknown).
     */
    fun resetCard(context: CardContext): CardContext

    /**
     * Deletes the card.
     */
    fun deleteCard(context: CardContext): CardContext

}