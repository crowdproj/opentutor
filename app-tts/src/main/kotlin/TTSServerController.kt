package com.gitlab.sszuev.flashcards.speaker

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class TTSServerController(
    private val processor: TTSServerProcessor
) {

    private val scope = CoroutineScope(
        context = Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("thread-tts-controller")
    )

    /**
     * Runs the job asynchronously.
     * @return [Job]
     */
    fun start() = scope.launch {
        processor.process(Dispatchers.IO)
    }
}