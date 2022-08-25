package com.gitlab.sszuev.flashcards.core.utils

import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.domain.*

fun CardEntity.normalize(): CardEntity {
    return CardEntity(
        cardId = this.cardId.normalize(),
        dictionaryId = this.dictionaryId.normalize(),
        word = this.word.trim(),
        transcription = this.transcription?.trim(),
        partOfSpeech = this.partOfSpeech?.lowercase()?.trim(),
        details = this.details,
        answered = this.answered,
        translations = this.translations,
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
        details = this.details,
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