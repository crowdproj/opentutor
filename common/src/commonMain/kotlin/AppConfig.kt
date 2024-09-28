package com.gitlab.sszuev.flashcards

import kotlinx.serialization.Serializable

/**
 * Contains various system settings, passed from environment or system properties on application start.
 */
@Serializable
data class AppConfig(
    val createBuiltinDictionariesOnFirstLogin: Boolean,
    val defaultNumberOfRightAnswers: Int,
    val defaultStageShowNumberOfWords: Int,
    val defaultNumberOfWordsPerStage: Int,
    val defaultStageOptionsNumberOfVariants: Int,
) {
    companion object {
        val DEFAULT = AppConfig(
            defaultNumberOfRightAnswers = 10,
            defaultStageShowNumberOfWords = 10,
            defaultNumberOfWordsPerStage = 5,
            defaultStageOptionsNumberOfVariants = 6,
            createBuiltinDictionariesOnFirstLogin = true,
        )
    }
}