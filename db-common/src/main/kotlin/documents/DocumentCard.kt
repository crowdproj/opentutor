package com.gitlab.sszuev.flashcards.common.documents

data class DocumentCard(
    val id: Long?,
    val text: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val details: String = "{}",
    val answered: Int? = null,
    val translations: List<String>,
    val examples: List<String> = emptyList(),
    val status: CardStatus,
)