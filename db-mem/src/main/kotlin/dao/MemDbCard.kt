package com.gitlab.sszuev.flashcards.dbmem.dao

data class MemDbCard(
    val id: Long,
    val dictionaryId: Long,
    val text: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val details: String = "{}",
    val answered: Int? = null,
    val translations: List<String>,
    val examples: List<String> = emptyList(),
)

