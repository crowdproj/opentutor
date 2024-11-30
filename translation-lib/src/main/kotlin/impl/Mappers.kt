package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TCard
import com.gitlab.sszuev.flashcards.translation.api.TExample
import com.gitlab.sszuev.flashcards.translation.api.TWord

fun List<LingueeEntry>.toTCard(): TCard = TCard(map { it.toTWord() })

private fun LingueeEntry.toTWord() = TWord(
    partOfSpeech = this.pos,
    word = this.text,
    translations = this.translations.toTTranslations(),
    examples = this.translations.flatMap { it.toTExamples() }
)

private fun List<LingueeTranslation>.toTTranslations(): List<List<String>> {
    return filterNot { it.text.isNullOrBlank() }.map { listOf(checkNotNull(it.text)) }
}

private fun LingueeTranslation.toTExamples() = (this.examples ?: emptyList()).map { it.toTExample() }

private fun LingueeExample.toTExample() = TExample(text = src, translation = dst)