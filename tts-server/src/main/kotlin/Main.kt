package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.speaker.controllers.TextToSpeechController
import com.gitlab.sszuev.flashcards.speaker.impl.NoOpTextToSpeechService

fun main() {
    TextToSpeechController(NoOpTextToSpeechService).start()
}