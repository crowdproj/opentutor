package com.gitlab.sszuev.flashcards.speaker.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface TextToSpeechProcessor {
    /**
     * Runs the endless lifecycle, which processes TTS-messages.
     * @param [dispatcher][CoroutineScope]
     */
    suspend fun process(dispatcher: CoroutineContext = Dispatchers.IO)
}