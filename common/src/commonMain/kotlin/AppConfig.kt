package com.gitlab.sszuev.flashcards

data class AppConfig(val numberOfRightAnswers: Int) {
    companion object {
        val DEFAULT = AppConfig(10)
    }
}
