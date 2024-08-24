package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.mappers.toCardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.core.processes.SearchCardsHelper")

/**
 * oldest cards go first
 */
private val oldestFirstComparator: Comparator<CardEntity> = Comparator<CardEntity> { left, right ->
    left.changedAt.compareTo(right.changedAt)
}

/**
 * Prepares a card deck for a tutor-session.
 */
internal fun CardContext.findCardDeck(dictionaries: Map<DictionaryId, DictionaryEntity>): List<CardEntity> {
    if (logger.isDebugEnabled) {
        logger.debug("Search cards request: {}", this.normalizedRequestCardFilter)
    }
    val thresholds = dictionaries.mapKeys { it.key.asString() }.mapValues { it.value.numberOfRightAnswers }
    val foundCards = this.repositories.cardRepository
        .findCardsByDictionaryIdIn(this.normalizedRequestCardFilter.dictionaryIds.map { it.asString() })
        .filter {
            !this.normalizedRequestCardFilter.onlyUnknown
                || (it.answered ?: -1) < (thresholds[it.dictionaryId] ?: config.numberOfRightAnswers)
        }
        .map { it.toCardEntity() }

    // prepares the collection so that the resulting list contains cards from different dictionaries
    val cardsByDictionary = foundCards.groupBy { it.dictionaryId }.mapValues {
        var value = it.value
        if (this.normalizedRequestCardFilter.random) {
            value = value.shuffled()
        }
        val latestFirst = value.sortedWith(oldestFirstComparator.reversed())
        val oldestFirst = value.sortedWith(oldestFirstComparator)
        value = latestFirst.zip(oldestFirst).flatMap { p -> listOf(p.first, p.second) }
        value.toMutableList()
    }.toMutableMap()
    val selectedCards = mutableListOf<CardEntity>()
    while (cardsByDictionary.isNotEmpty()) {
        cardsByDictionary.keys.toSet().forEach { k ->
            val v = checkNotNull(cardsByDictionary[k])
            if (v.isEmpty()) {
                cardsByDictionary.remove(k)
            } else {
                selectedCards.add(v.removeFirst())
            }
        }
    }

    if (!this.normalizedRequestCardFilter.random && this.normalizedRequestCardFilter.length > 0) {
        val res = selectedCards.take(this.normalizedRequestCardFilter.length).toList()
        if (logger.isDebugEnabled) {
            logger.debug("Search cards response: {}", res)
        }
        return res
    }

    // prepare card's deck so that it contains non-similar words
    var res = selectedCards.toList()
    if (this.normalizedRequestCardFilter.length > 0) {
        val set = mutableSetOf<CardEntity>()
        collectCardDeck(res, set, this.normalizedRequestCardFilter.length)
        res = set.toList()
        if (this.normalizedRequestCardFilter.random) {
            res = res.shuffled()
        }
    }
    if (logger.isDebugEnabled) {
        logger.debug("Search cards response: {}", res)
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
    res.addAll(rest.sortedWith(oldestFirstComparator).take(num - res.size))
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
