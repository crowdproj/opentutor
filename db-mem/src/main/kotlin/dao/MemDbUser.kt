package com.gitlab.sszuev.flashcards.dbmem.dao

import java.time.LocalDateTime

data class MemDbUser(
    val id: String,
    val createdAt: LocalDateTime,
    val details: Map<String, Any> = emptyMap(),
)