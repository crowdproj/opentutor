package com.gitlab.sszuev.flashcards.speaker.controllers

import com.gitlab.sszuev.flashcards.speaker.rabbitmq.ConnectionConfig
import com.gitlab.sszuev.flashcards.speaker.rabbitmq.ProcessorConfig
import com.gitlab.sszuev.flashcards.speaker.rabbitmq.TextToSpeechProcessorImpl
import com.gitlab.sszuev.flashcards.speaker.services.TextToSpeechService
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class TextToSpeechController(
    private val processor: TextToSpeechProcessor
) {

    constructor(
        service: TextToSpeechService,
        connectionConfig: ConnectionConfig = ConnectionConfig(),
        processorConfig: ProcessorConfig = ProcessorConfig()
    ) : this(
        TextToSpeechProcessorImpl(
            service = service,
            connectionConfig = connectionConfig,
            config = processorConfig
        )
    )

    private val scope = CoroutineScope(
        context = Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("thread-rabbitmq-controller")
    )

    /**
     * Runs job asynchronously.
     * @return [Job]
     */
    fun start() = scope.launch {
        processor.process(Dispatchers.IO)
    }
}