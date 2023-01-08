package com.gitlab.sszuev.flashcards.dbmem.dao

data class MemDbExample(
    val text: String,
    val translation: String? = null,
)