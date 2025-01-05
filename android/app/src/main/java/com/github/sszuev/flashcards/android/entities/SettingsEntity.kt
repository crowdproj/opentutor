package com.github.sszuev.flashcards.android.entities

import kotlinx.serialization.Serializable

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
)