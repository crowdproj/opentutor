package com.gitlab.sszuev.flashcards.speaker.controllers

import com.gitlab.sszuev.flashcards.speaker.rabbitmq.ConnectionConfig
import com.gitlab.sszuev.flashcards.speaker.rabbitmq.QueueConfig
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
        queueConfig: QueueConfig = QueueConfig()
    ) : this(
        TextToSpeechProcessorImpl(
            service = service,
            connectionConfig = connectionConfig,
            queueConfig = queueConfig
        )
    )

    private val scope = CoroutineScope(
        context = Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("thread-rabbitmq-controller")
    )

    fun start() = scope.launch {
        processor.process(Dispatchers.IO)
    }
}