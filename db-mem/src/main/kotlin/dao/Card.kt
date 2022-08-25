package com.gitlab.sszuev.flashcards.dbmem.dao

data class Card(
    val id: Long,
    val dictionaryId: Long,
    val text: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val details: String = "{}",
    val answered: Int? = null,
    val translations: List<Translation>,
    val examples: List<Example> = emptyList(),
)

