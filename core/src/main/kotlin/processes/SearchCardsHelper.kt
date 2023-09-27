package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse

/**
 * Prepares a card deck for a tutor-session.
 */
fun CardContext.findCardDeck(): CardsDbResponse {
    val userId = this.contextUserEntity.id
    return this.repositories.cardRepository(this.workMode).searchCard(userId, this.normalizedRequestCardFilter)
}