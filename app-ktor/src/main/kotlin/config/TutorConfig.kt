package com.gitlab.sszuev.flashcards.config

import io.ktor.server.config.ApplicationConfig

data class TutorConfig(
    val numberOfWordsToShow: Int,
    val numberOfRightAnswers: Int,
    val numberOfWordsPerStage: Int,
    val numberOfOptionsPerWord: Int,
    val useBuiltinDictionaries: Boolean,
) {

    constructor(config: ApplicationConfig) : this(
        numberOfWordsToShow = config.getPositiveInt("app.tutor.run.words-for-show", 10),
        numberOfRightAnswers = config.getPositiveInt("app.tutor.run.answers", 10),
        numberOfWordsPerStage = config.getPositiveInt("app.tutor.run.words-for-test", 5),
        numberOfOptionsPerWord = config.getPositiveInt("app.tutor.run.stage.option-variants", 6),
        useBuiltinDictionaries = config.getBoolean("app.tutor.create-builtin-dictionaries", false),
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
