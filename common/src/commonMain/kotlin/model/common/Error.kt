package com.gitlab.sszuev.flashcards.model.common

data class Error (
    val code: String = "",
    val group: String = "",
    val field: String = "",
    val message: String = "",
    val exception: Throwable? = null,
)