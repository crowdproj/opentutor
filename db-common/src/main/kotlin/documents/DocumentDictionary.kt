package com.gitlab.sszuev.flashcards.common.documents

data class DocumentDictionary(
    val name: String,
    val sourceLang: String,
    val targetLang: String,
    val cards: List<DocumentCard>,
)