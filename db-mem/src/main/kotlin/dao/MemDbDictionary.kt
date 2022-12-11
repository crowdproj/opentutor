package com.gitlab.sszuev.flashcards.dbmem.dao

import java.time.LocalDateTime

data class MemDbDictionary(
    val name: String,
    val sourceLanguage: MemDbLanguage,
    val targetLanguage: MemDbLanguage,
    val details: Map<String, String> = emptyMap(),
    val userId: Long? = null,
    val id: Long? = null,
    val changedAt: LocalDateTime? = null,
)