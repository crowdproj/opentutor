package com.github.sszuev.flashcards.android.repositories

import kotlinx.serialization.Serializable

@Serializable
data class SettingsResource (
    val stageShowNumberOfWords: Int? = null,
    val stageOptionsNumberOfVariants: Int? = null,
    val numberOfWordsPerStage: Int? = null,
    val stageMosaicSourceLangToTargetLang: Boolean? = null,
    val stageOptionsSourceLangToTargetLang: Boolean? = null,
    val stageWritingSourceLangToTargetLang: Boolean? = null,
    val stageSelfTestSourceLangToTargetLang: Boolean? = null,
    val stageMosaicTargetLangToSourceLang: Boolean? = null,
    val stageOptionsTargetLangToSourceLang: Boolean? = null,
    val stageWritingTargetLangToSourceLang: Boolean? = null,
    val stageSelfTestTargetLangToSourceLang: Boolean? = null,
)