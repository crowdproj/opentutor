package com.gitlab.sszuev.flashcards.dbmem

data class AppConfig(
    val numberOfRightAnswers: Int = Settings.numberOfRightAnswers,
    val dataLocation: String = Settings.dataLocation,
)