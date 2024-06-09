package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.NONE
import kotlinx.datetime.Instant

data class DbUser(val id: String, val createdAt: Instant = Instant.NONE)