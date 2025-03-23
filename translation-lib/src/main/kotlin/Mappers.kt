package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationExample

internal fun LingueeEntry.toTWord() = TranslationEntity(
    partOfSpeech = this.pos,
    word = this.text,
    translations = this.translations.toTTranslations(),
    examples = this.translations.flatMap { it.toTExamples() }
)

private fun List<LingueeEntryTranslation>.toTTranslations(): List<List<String>> {
    return listOf(filterNot { it.text.isNullOrBlank() }.map { checkNotNull(it.text) })
}

private fun LingueeEntryTranslation.toTExamples() = (this.examples ?: emptyList()).map { it.toTExample() }

private fun LingueeEntryExample.toTExample() = TranslationExample(text = src, translation = dst)

internal fun YandexEntry.toTWords() = this.def.map { it.toTEntity() }

private fun YandexEntryDefinition.toTEntity(): TranslationEntity = TranslationEntity(
    word = this.text,
    partOfSpeech = this.pos,
    transcription = this.ts,
    translations = listOf(this.tr.map { it.text })
)