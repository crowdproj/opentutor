package com.gitlab.sszuev.flashcards

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val numberOfRightAnswers: Int,
    val createBuiltinDictionariesOnFirstLogin: Boolean,
) {
    companion object {
        val DEFAULT = AppConfig(
            numberOfRightAnswers = 10,
            createBuiltinDictionariesOnFirstLogin = true,
        )
    }
}