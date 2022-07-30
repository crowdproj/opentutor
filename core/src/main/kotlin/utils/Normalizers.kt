package com.gitlab.sszuev.flashcards.core.utils

import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.domain.*

fun CardEntity.normalize(): CardEntity {
    return CardEntity(
        cardId = this.cardId.normalize(),
        dictionaryId = this.dictionaryId.normalize(),
        word = this.word.trim(),
        transcription = this.transcription?.trim(),
        partOfSpeech = this.partOfSpeech?.trim(),
        details = this.details?.trim() ?: "{}",
        answered = this.answered,
        translations = this.translations.flatMap { splitIntoWords(it) },
        examples = this.examples.asSequence().map { it.trim() }.filter { it.isNotBlank() }.toList(),
    )
}

fun CardFilter.normalize(): CardFilter {
    return CardFilter(
        dictionaryIds = this.dictionaryIds.map { it.normalize() },
        random = this.random,
        length = this.length,
        withUnknown = this.withUnknown,
    )
}

fun CardLearn.normalize(): CardLearn {
    return CardLearn(
        cardId = this.cardId.normalize(),
        details = this.details.mapKeys { it.key.trim() } // empty strings will be lost
    )
}

fun ResourceGet.normalize(): ResourceGet {
    return ResourceGet(
        word = this.word.trim(),
        lang = this.lang.normalize(),
    )
}

fun CardId.normalize(): CardId {
    return CardId(this.normalizeAsString())
}

fun DictionaryId.normalize(): DictionaryId {
    return DictionaryId(this.normalizeAsString())
}

fun LangId.normalize(): LangId {
    return LangId(this.normalizeAsString().lowercase())
}

private fun Id.normalizeAsString(): String {
    return asString().trim()
}

/**
 * Splits the given `phrase` using comma (i.e. '`,`') as separator.
 * Commas inside the parentheses (e.g. "`(x,y)`") are not considered.
 *
 * @param [phrase]
 * @return [List]
 */
internal fun splitIntoWords(phrase: String): List<String> {
    val parts = phrase.split(",")
    val res = mutableListOf<String>()
    var i = 0
    while (i < parts.size) {
        val pi = parts[i].trim()
        if (pi.isEmpty()) {
            i++
            continue
        }
        if (!pi.contains("(") || pi.contains(")")) {
            res.add(pi)
            i++
            continue
        }
        val sb = StringBuilder(pi)
        var j = i + 1
        while (j < parts.size) {
            val pj = parts[j].trim { it <= ' ' }
            if (pj.isEmpty()) {
                j++
                continue
            }
            sb.append(", ").append(pj)
            if (pj.contains(")")) {
                break
            }
            j++
        }
        if (sb.lastIndexOf(")") == -1) {
            res.add(pi)
            i++
            continue
        }
        res.add(sb.toString())
        i = j
        i++
    }
    return res
}
