package com.gitlab.sszuev.flashcards.dbmem.dao

data class Dictionary(
    val id: Long,
    val name: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val cards: List<Card>,
)