package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.NONE
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class DbUser(
    val id: String,
    val createdAt: Instant = Instant.NONE,
    val details: Map<String, Any> = emptyMap(),
)