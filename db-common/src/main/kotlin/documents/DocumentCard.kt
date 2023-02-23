package com.gitlab.sszuev.flashcards.common.documents

data class DocumentCard(
    val text: String,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val translations: List<String>,
    val examples: List<String> = emptyList(),
    val status: DocumentCardStatus,
)