package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationExample

internal fun LingueeEntry.toTWord() = TranslationEntity(
    partOfSpeech = this.pos,
    word = this.text,
    translations = this.translations.toTTranslations(),
    examples = this.translations.flatMap { it.toTExamples() }
)

private fun List<LingueeTranslation>.toTTranslations(): List<List<String>> {
    return filterNot { it.text.isNullOrBlank() }.map { listOf(checkNotNull(it.text)) }
}

private fun LingueeTranslation.toTExamples() = (this.examples ?: emptyList()).map { it.toTExample() }

private fun LingueeExample.toTExample() = TranslationExample(text = src, translation = dst)