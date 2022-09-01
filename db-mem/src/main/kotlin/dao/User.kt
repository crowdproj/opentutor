package com.gitlab.sszuev.flashcards.dbmem.dao

import java.util.*

data class User(
    val id: Long,
    val uuid: UUID,
    val role: Int,
)