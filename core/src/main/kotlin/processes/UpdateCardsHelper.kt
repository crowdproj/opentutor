@file:OptIn(ExperimentalTime::class)

package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.mappers.toCardEntity
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import kotlin.time.ExperimentalTime

internal fun CardContext.learnCards(
    foundCards: Iterable<CardEntity>,
    cardLearns: Map<CardId, CardLearn>
): List<CardEntity> {
    val cards = foundCards.map { card ->
        val learn = checkNotNull(cardLearns[card.cardId])
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
        card.copy(stats = details, answered = answered.toInt()).toDbCard()
    }
    return this.repositories.cardRepository.updateCards(cards).map { it.toCardEntity() }
}

internal fun CardContext.resetCards(
    foundCards: Iterable<CardEntity>,
): List<CardEntity> {

    val cards = foundCards.map { card ->
        card.copy(answered = 0, details = emptyMap()).toDbCard()
    }
    return this.repositories.cardRepository.updateCards(cards).map { it.toCardEntity() }
}