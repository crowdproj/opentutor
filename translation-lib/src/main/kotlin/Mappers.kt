package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity

internal fun YandexEntry.toTWords() = this.def.map { it.toTEntity() }

private fun YandexEntryDefinition.toTEntity(): TranslationEntity = TranslationEntity(
    word = this.text,
    partOfSpeech = this.pos,
    transcription = this.ts,
    translations = listOf(this.tr.map { it.text })
)