package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.AppContext

interface CardService {

    /**
     * Creates a card-entity.
     */
    fun createCardEntity(context: AppContext): AppContext
}