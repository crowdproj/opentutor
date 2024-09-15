package com.gitlab.sszuev.flashcards.model.domain

import kotlinx.serialization.Serializable

/**
 * User's settings.
 */
@Serializable
data class SettingsEntity(
    val numberOfWordsPerStage: Int,
    val stageShowNumberOfWords: Int,
    val stageOptionsNumberOfVariants: Int,
) {
    companion object {
        val DEFAULT = SettingsEntity(
            numberOfWordsPerStage = 5,
            stageShowNumberOfWords = 10,
            stageOptionsNumberOfVariants = 6
        )
    }
}
