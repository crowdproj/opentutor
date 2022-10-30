package com.gitlab.sszuev.flashcards.dbmem.dao

data class MemDbDictionary(
    val userId: Long?,
    val id: Long,
    val name: String,
    val sourceLanguage: MemDbLanguage,
    val targetLanguage: MemDbLanguage,
    val cards: MutableMap<Long, MemDbCard>,
)