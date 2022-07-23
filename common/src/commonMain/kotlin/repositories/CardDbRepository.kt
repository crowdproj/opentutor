package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardLock
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

/**
 * Database repository to work with cards.
 */
interface CardDbRepository {
    /**
     * Gets all cards by dictionaryId.
     */
    fun getAllCards(request: DictionaryIdDbRequest): CardEntitiesDbResponse
}

data class DictionaryIdDbRequest(val dictionaryId: DictionaryId, val lock: CardLock)

data class CardEntitiesDbResponse(val cards: List<CardEntity>, val errors: List<AppError>)