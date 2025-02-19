package com.gitlab.sszuev.flashcards.config

import io.ktor.server.config.ApplicationConfig

data class TutorConfig(
    val numberOfWordsToShow: Int,
    val numberOfRightAnswers: Int,
    val numberOfWordsPerStage: Int,
    val numberOfOptionsPerWord: Int,

    val showStageMosaicSourceToTarget: Boolean,
    val showStageMosaicTargetToSource: Boolean,
    val showStageOptionsSourceToTarget: Boolean,
    val showStageOptionsTargetToSource: Boolean,
    val showStageWritingSourceToTarget: Boolean,
    val showStageWritingTargetToSource: Boolean,
    val showStageSelfTestSourceToTarget: Boolean,
    val showStageSelfTestTargetToSource: Boolean,
) {

    constructor(config: ApplicationConfig) : this(
        numberOfWordsToShow = config.getPositiveInt("app.tutor.run.words-for-show", 10),
        numberOfRightAnswers = config.getPositiveInt("app.tutor.run.answers", 10),
        numberOfWordsPerStage = config.getPositiveInt("app.tutor.run.words-for-test", 5),
        numberOfOptionsPerWord = config.getPositiveInt("app.tutor.run.stage.option-variants", 6),
        showStageOptionsSourceToTarget = config.getBoolean("app.tutor.run.stage.option-source-to-target", true),
        showStageOptionsTargetToSource = config.getBoolean("app.tutor.run.stage.option-target-to-source", false),
        showStageMosaicSourceToTarget = config.getBoolean("app.tutor.run.stage.mosaic-source-to-target", true),
        showStageMosaicTargetToSource = config.getBoolean("app.tutor.run.stage.mosaic-target-to-source", false),
        showStageWritingSourceToTarget = config.getBoolean("app.tutor.run.stage.writing-source-to-target", true),
        showStageWritingTargetToSource = config.getBoolean("app.tutor.run.stage.writing-target-to-source", false),
        showStageSelfTestSourceToTarget = config.getBoolean("app.tutor.run.stage.self-test-source-to-target", true),
        showStageSelfTestTargetToSource = config.getBoolean("app.tutor.run.stage.self-test-target-to-source", false),
    )
}

private fun ApplicationConfig.getPositiveInt(key: String, default: Int): Int {
    val value = propertyOrNull(key)?.getString() ?: return default
    require(value.matches("\\d+".toRegex()) && value.toInt() > 0)
    return value.toInt()
}

private fun ApplicationConfig.getBoolean(key: String, default: Boolean): Boolean {
    return propertyOrNull(key)?.getString()?.toBoolean() ?: return default
}
