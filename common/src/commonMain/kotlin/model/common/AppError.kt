package com.gitlab.sszuev.flashcards.model.common

data class AppError (
    val code: String = "",
    val group: String = "",
    val field: String = "",
    val message: String = "",
    val exception: Throwable? = null,
)
