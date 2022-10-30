package com.gitlab.sszuev.flashcards.common.documents

class DocumentDictionary(
    val id: Long?,
    val userId: Long?,
    val name: String,
    val sourceLang: DocumentLang,
    val targetLang: DocumentLang,
    val cards: List<DocumentCard>,
)