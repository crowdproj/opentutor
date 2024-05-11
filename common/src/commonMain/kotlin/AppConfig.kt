package com.gitlab.sszuev.flashcards

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(val numberOfRightAnswers: Int) {
    companion object {
        val DEFAULT = AppConfig(10)
    }
}