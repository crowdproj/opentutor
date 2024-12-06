package com.gitlab.sszuev.flashcards.translation.api

data class TranslationEntity(
    val word: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val examples: List<TranslationExample> = emptyList(),
    val translations: List<List<String>> = emptyList(),
)

data class TranslationExample(
    val text: String,
    val translation: String? = null,
)