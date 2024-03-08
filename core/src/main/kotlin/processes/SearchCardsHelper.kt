package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse

private val comparator: Comparator<CardEntity> = Comparator<CardEntity> { left, right ->
    val la = left.answered ?: 0
    val ra = right.answered ?: 0
    la.compareTo(ra)
}.thenComparing { lc, rc ->
    lc.changedAt.compareTo(rc.changedAt)
}

/**
 * Prepares a card deck for a tutor-session.
 */
fun CardContext.findCardDeck(): CardsDbResponse {
    return if (this.normalizedRequestCardFilter.random) {
        // For random mode, do not use the database support since logic is quite complicated.
        // We load everything into memory, since the dictionary can hardly contain more than a thousand words,
        // i.e., this is relatively small data.
        val filter = this.normalizedRequestCardFilter.copy(random = false, length = -1)
        val res = this.repositories.cardRepository(this.workMode).searchCard(this.contextUserEntity.id, filter)
        if (res.errors.isNotEmpty()) {
            return res
        }
        var cards = res.cards.shuffled().sortedWith(comparator)
        if (this.normalizedRequestCardFilter.length > 0) {
            val set = mutableSetOf<CardEntity>()
            collectCardDeck(cards, set, this.normalizedRequestCardFilter.length)
            cards = set.toList()
            cards = cards.shuffled()
        }
        return res.copy(cards = cards)
    } else {
        this.repositories.cardRepository(this.workMode)
            .searchCard(this.contextUserEntity.id, this.normalizedRequestCardFilter)
    }
}

private fun collectCardDeck(all: List<CardEntity>, res: MutableSet<CardEntity>, num: Int) {
    if (all.size <= num) {
        res.addAll(all)
        return
    }
    val rest = mutableListOf<CardEntity>()
    all.forEach { candidate ->
        if (isSimilar(candidate, res)) {
            rest.add(candidate)
        } else {
            res.add(candidate)
            if (res.size == num) {
                return
            }
        }
    }
    res.addAll(rest.sortedWith(comparator).take(num - res.size))
}

internal fun isSimilar(candidate: CardEntity, res: Set<CardEntity>): Boolean = res.any { it.isSimilar(candidate) }

internal fun CardEntity.isSimilar(other: CardEntity): Boolean =
    if (this == other) true else this.words.isSimilar(other.words)

private fun List<CardWordEntity>.isSimilar(other: List<CardWordEntity>): Boolean {
    forEach { left ->
        other.forEach { right ->
            if (left.isSimilar(right)) {
                return true
            }
        }
    }
    return false
}

private fun CardWordEntity.isSimilar(other: CardWordEntity): Boolean {
    if (this == other) {
        return true
    }
    if (word.isSimilar(other.word)) {
        return true
    }
    val otherTranslations = other.translations.flatten()
    translations.flatten().forEach { left ->
        otherTranslations.forEach { right ->
            if (left.isSimilar(right)) {
                return true
            }
        }
    }
    return false
}

private fun String.isSimilar(other: String): Boolean {
    if (this == other) {
        return true
    }
    return prefix(3) == other.prefix(3)
}

private fun String.prefix(num: Int): String {
    if (length <= num) {
        return lowercase()
    }
    return substring(0, num).lowercase()
}
