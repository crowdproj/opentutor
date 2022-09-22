package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService

object NoOpTextToSpeechService : TextToSpeechService {
    override fun getResource(id: String, vararg args: String?): ByteArray {
        error("Must not be called.")
    }
}