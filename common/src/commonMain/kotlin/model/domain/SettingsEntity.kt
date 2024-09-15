package com.gitlab.sszuev.flashcards.model.domain

import kotlinx.serialization.Serializable

/**
 * User's settings.
 */
@Serializable
data class SettingsEntity(
    val stageShowNumberOfWords: Int,
    val numberOfWordsPerStage: Int,
    val stageOptionsNumberOfVariants: Int,
) {
    companion object {
        val DEFAULT = SettingsEntity(
            stageShowNumberOfWords = 10,
            numberOfWordsPerStage = 5,
            stageOptionsNumberOfVariants = 6
        )
    }
}
