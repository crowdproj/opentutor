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

fun DictionaryId.toDbRequest(): DictionaryIdDbRequest {
    return DictionaryIdDbRequest(this)
}

data class DictionaryIdDbRequest(val id: DictionaryId, val lock: CardLock = CardLock.NONE)

data class CardEntitiesDbResponse(val cards: List<CardEntity>, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = CardEntitiesDbResponse(cards = emptyList(), errors = emptyList())
    }
}