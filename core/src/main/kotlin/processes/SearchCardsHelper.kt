package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity

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
internal fun CardContext.findCardDeck(): List<CardEntity> {
    val threshold = config.numberOfRightAnswers
    var cards = this.repositories.cardRepository(this.workMode)
        .findCardsByDictionaryIdIn(this.normalizedRequestCardFilter.dictionaryIds)
        .filter { !this.normalizedRequestCardFilter.onlyUnknown || (it.answered ?: -1) <= threshold }
    if (this.normalizedRequestCardFilter.random) {
        cards = cards.shuffled()
    }
    cards = cards.sortedWith(comparator)
    if (!this.normalizedRequestCardFilter.random && this.normalizedRequestCardFilter.length > 0) {
        return cards.take(this.normalizedRequestCardFilter.length).toList()
    }
    var res = cards.toList()
    if (this.normalizedRequestCardFilter.length > 0) {
        val set = mutableSetOf<CardEntity>()
        collectCardDeck(res, set, this.normalizedRequestCardFilter.length)
        res = set.toList()
        if (this.normalizedRequestCardFilter.random) {
            res = res.shuffled()
        }
    }
    return res
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
