package com.gitlab.sszuev.flashcards.speaker.services

class DummyTextToSpeechServiceImpl : TextToSpeechService {

    override fun getResource(id: String): ByteArray {
        return ByteArray(0)
    }
}