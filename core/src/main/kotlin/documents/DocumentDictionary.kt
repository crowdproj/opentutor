package com.gitlab.sszuev.flashcards.core.documents

/**
 * Lingvo document.
 */
data class DocumentDictionary(
    val name: String,
    val sourceLang: String,
    val targetLang: String,
    val cards: List<DocumentCard>,
)