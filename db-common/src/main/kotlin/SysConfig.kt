package com.gitlab.sszuev.flashcards.common

data class SysConfig(
    val numberOfRightAnswers: Int = TutorSettings.numberOfRightAnswers,
)