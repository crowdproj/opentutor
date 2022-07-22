package com.gitlab.sszuev.flashcards.dbmem.dao

data class Translation(
    val id: Long,
    val cardId: Long,
    val text: String,
)