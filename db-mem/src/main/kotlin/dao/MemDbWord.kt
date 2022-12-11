package com.gitlab.sszuev.flashcards.dbmem.dao

data class MemDbWord(
    val word: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val examples: List<MemDbExample> = emptyList(),
    val translations: List<List<String>> = emptyList(),
)