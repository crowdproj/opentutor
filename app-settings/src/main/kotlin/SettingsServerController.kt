package com.gitlab.sszuev.flashcards.settings

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class SettingsServerController(
    private val processor: SettingsServerProcessor,
) {
    private val scope = CoroutineScope(
        context = Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("thread-settings-controller")
    )

    /**
     * Runs the job asynchronously.
     * @return [Job]
     */
    fun start() = scope.launch {
        processor.process(Dispatchers.IO)
    }
}