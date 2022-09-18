package com.gitlab.sszuev.flashcards.config

import io.ktor.server.config.*

data class TutorConfig(
    val numberOfWordsToShow: String,
    val numberOfRightAnswers: String,
    val numberOfWordsPerStage: String,
    val numberOfOptionsPerWord: String,
) {

    constructor(config: ApplicationConfig): this(
        numberOfWordsToShow = config.property("app.tutor.run.words-for-show").getString(),
        numberOfRightAnswers = config.property("app.tutor.run.answers").getString(),
        numberOfWordsPerStage = config.property("app.tutor.run.words-for-test").getString(),
        numberOfOptionsPerWord = config.property("app.tutor.run.stage.option-variants").getString(),
    )

    init {
        require(testSetting(numberOfWordsToShow))
        require(testSetting(numberOfRightAnswers))
        require(testSetting(numberOfWordsPerStage))
        require(testSetting(numberOfOptionsPerWord))
    }

    private fun testSetting(value: String): Boolean {
        return value.matches("\\d+".toRegex()) && value.toInt() > 0
    }
}
