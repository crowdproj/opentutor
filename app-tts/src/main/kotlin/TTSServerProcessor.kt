package com.gitlab.sszuev.flashcards.speaker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface TTSServerProcessor {
    /**
     * Runs the endless lifecycle, which processes TTS messages.
     * @param [coroutineContext][CoroutineScope]
     */
    suspend fun process(coroutineContext: CoroutineContext = Dispatchers.IO)
}