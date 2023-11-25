package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse

/**
 * Prepares a card deck for a tutor-session.
 */
fun CardContext.findCardDeck(): CardsDbResponse {
    return if (this.normalizedRequestCardFilter.random) {
        // For random mode, do not use the database support since logic is quite complicated.
        // We load everything into memory, since the dictionary can hardly contain more than a thousand words,
        // i.e., this is quite small data.
        //TODO: implement new logic
        return this.repositories.cardRepository(this.workMode)
            .searchCard(this.contextUserEntity.id, this.normalizedRequestCardFilter)
    } else {
        this.repositories.cardRepository(this.workMode)
            .searchCard(this.contextUserEntity.id, this.normalizedRequestCardFilter)
    }
}