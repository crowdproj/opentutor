package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.speaker.controllers.TextToSpeechController

fun main() {
    TextToSpeechController(createTTSService()).start()
}