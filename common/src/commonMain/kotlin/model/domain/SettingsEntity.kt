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
    val stageMosaicSourceLangToTargetLang: Boolean,
    val stageOptionsSourceLangToTargetLang: Boolean,
    val stageWritingSourceLangToTargetLang: Boolean,
    val stageSelfTestSourceLangToTargetLang: Boolean,
    val stageMosaicTargetLangToSourceLang: Boolean,
    val stageOptionsTargetLangToSourceLang: Boolean,
    val stageWritingTargetLangToSourceLang: Boolean,
    val stageSelfTestTargetLangToSourceLang: Boolean,
) {
    companion object {
        val DEFAULT = SettingsEntity(
            numberOfWordsPerStage = 5,
            stageShowNumberOfWords = 10,
            stageOptionsNumberOfVariants = 6,
            stageMosaicSourceLangToTargetLang = true,
            stageOptionsSourceLangToTargetLang = true,
            stageWritingSourceLangToTargetLang = true,
            stageSelfTestSourceLangToTargetLang = true,
            stageMosaicTargetLangToSourceLang = false,
            stageOptionsTargetLangToSourceLang = false,
            stageWritingTargetLangToSourceLang = false,
            stageSelfTestTargetLangToSourceLang = false,
        )
    }
}
