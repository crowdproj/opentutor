package com.gitlab.sszuev.flashcards.translation.api

data class TCard(
    val words: List<TWord> = emptyList(),
)

data class TWord(
    val word: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val examples: List<TExample> = emptyList(),
    val translations: List<List<String>> = emptyList(),
)

data class TExample(
    val text: String,
    val translation: String? = null,
)