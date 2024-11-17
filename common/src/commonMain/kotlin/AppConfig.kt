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

    val defaultStageMosaicSourceLangToTargetLang: Boolean,
    val defaultStageOptionsSourceLangToTargetLang: Boolean,
    val defaultStageWritingSourceLangToTargetLang: Boolean,
    val defaultStageSelfTestSourceLangToTargetLang: Boolean,

    val defaultStageMosaicTargetLangToSourceLang: Boolean,
    val defaultStageOptionsTargetLangToSourceLang: Boolean,
    val defaultStageWritingTargetLangToSourceLang: Boolean,
    val defaultStageSelfTestTargetLangToSourceLang: Boolean,
) {
    companion object {
        val DEFAULT = AppConfig(
            createBuiltinDictionariesOnFirstLogin = true,
            defaultNumberOfRightAnswers = 10,
            defaultStageShowNumberOfWords = 10,
            defaultNumberOfWordsPerStage = 5,
            defaultStageOptionsNumberOfVariants = 6,
            defaultStageMosaicSourceLangToTargetLang = true,
            defaultStageOptionsSourceLangToTargetLang = true,
            defaultStageWritingSourceLangToTargetLang = true,
            defaultStageSelfTestSourceLangToTargetLang = true,
            defaultStageMosaicTargetLangToSourceLang = false,
            defaultStageOptionsTargetLangToSourceLang = false,
            defaultStageWritingTargetLangToSourceLang = false,
            defaultStageSelfTestTargetLangToSourceLang = false,
        )
    }
}