package com.gitlab.sszuev.flashcards.dbmem.dao

import java.time.LocalDateTime
import java.util.UUID

data class MemDbUser(
    val id: Long?,
    val uuid: UUID,
    val details: Map<String, String> = emptyMap(),
    val changedAt: LocalDateTime? =  null,
)