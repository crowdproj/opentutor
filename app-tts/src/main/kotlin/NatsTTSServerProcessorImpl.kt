package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.core.TTSCorProcessor
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.ttsContextFromByteArray
import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.Nats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

private val logger = LoggerFactory.getLogger(NatsTTSServerProcessorImpl::class.java)

class NatsTTSServerProcessorImpl(
    private val topic: String,
    private val group: String,
    private val repository: TTSResourceRepository,
    connectionFactory: () -> Connection,
) : TTSServerProcessor, AutoCloseable {

    constructor(
        service: TextToSpeechService,
        topic: String = "TTS",
        group: String = "TTS",
        connectionUrl: String = "nats://localhost:4222",
    ) : this(
        topic = topic,
        group = group,
        repository = DirectTTSResourceRepository(service),
        connectionFactory = { Nats.connectReconnectOnConnect(connectionUrl) },
    )

    private val run = AtomicBoolean(false)
    private val connection by lazy {
        connectionFactory().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
        }
    }
    private val processor = TTSCorProcessor()

    override suspend fun process(coroutineContext: CoroutineContext) {
        val dispatcher = connection.createDispatcher { msg: Message ->
            CoroutineScope(coroutineContext).launch {
                val context = ttsContextFromByteArray(msg.data)
                context.repository = repository
                processor.execute(context)
                context.errors.forEach {
                    logger.error("$it")
                    it.exception?.let { ex ->
                        logger.error("Exception: ${ex.message}", ex)
                    }
                }
                connection.publish(msg.replyTo, context.toByteArray())
            }
        }
        dispatcher.subscribe(topic, group)
        run.set(true)
        while (run.get()) {
            runCatching {
                delay(timeMillis = 100)
            }.onFailure { ex ->
                if (ex is CancellationException) {
                    logger.info("The lifecycle job is canceled.")
                } else {
                    logger.error("Unexpected error", ex)
                }
            }
        }
    }

    fun ready() = run.get()

    override fun close() {
        logger.info("Close.")
        run.set(false)
        connection.close()
    }
}