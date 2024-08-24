package com.gitlab.sszuev.flashcards

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val numberOfRightAnswers: Int,
    val createBuiltinDictionariesOnFirstLogin: Boolean,
) {
    companion object {
        const val DEFAULT_NUMBER_OF_RIGHT_ANSWERS = 10
        val DEFAULT = AppConfig(
            numberOfRightAnswers = DEFAULT_NUMBER_OF_RIGHT_ANSWERS,
            createBuiltinDictionariesOnFirstLogin = true,
        )
    }
}