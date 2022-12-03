package com.gitlab.sszuev.flashcards.common.documents

data class DocumentDictionary(
    val id: Long?,
    val userId: Long?,
    val name: String,
    val sourceLang: DocumentLang,
    val targetLang: DocumentLang,
    val cards: List<DocumentCard>,
)