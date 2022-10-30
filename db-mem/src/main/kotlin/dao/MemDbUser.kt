package com.gitlab.sszuev.flashcards.dbmem.dao

import java.util.*

data class MemDbUser(
    val id: Long,
    val uuid: UUID,
    val role: Int,
)