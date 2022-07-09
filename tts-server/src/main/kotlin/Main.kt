package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.speaker.controllers.TextToSpeechController
import com.gitlab.sszuev.flashcards.speaker.services.DummyTextToSpeechServiceImpl

fun main() {
    TextToSpeechController(DummyTextToSpeechServiceImpl()).start()
}