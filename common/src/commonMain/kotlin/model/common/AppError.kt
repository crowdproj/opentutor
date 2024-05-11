package com.gitlab.sszuev.flashcards.model.common

import com.gitlab.sszuev.flashcards.utils.ThrowableSerializer
import kotlinx.serialization.Serializable

@Serializable
data class AppError (
    val code: String = "",
    val group: String = "",
    val field: String = "",
    val message: String = "",
    @Serializable(with = ThrowableSerializer::class)
    val exception: Throwable? = null,
)
