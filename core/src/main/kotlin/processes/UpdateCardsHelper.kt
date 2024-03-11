package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse

fun CardContext.learnCards(): CardsDbResponse {
    val cards = this.normalizedRequestCardLearnList.associateBy { it.cardId }
    return this.repositories.cardRepository(this.workMode).updateCards(this.contextUserEntity.id, cards.keys) { card ->
        val learn = checkNotNull(cards[card.cardId])
        var answered = card.answered?.toLong() ?: 0L
        val details = card.stats.toMutableMap()
        learn.details.forEach {
            answered += it.value.toInt()
            require(answered < Int.MAX_VALUE && answered > Int.MIN_VALUE)
            if (answered < 0) {
                answered = 0
            }
            details.merge(it.key, it.value) { a, b -> a + b }
        }
        card.copy(stats = details, answered = answered.toInt())
    }
}