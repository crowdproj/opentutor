package com.gitlab.sszuev.flashcards.speaker

import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.Nats
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

private val logger = LoggerFactory.getLogger(NatsTextToSpeechProcessorImpl::class.java)

class NatsTextToSpeechProcessorImpl(
    private val service: TextToSpeechService,
    private val topic: String,
    private val group: String,
    connectionFactory: () -> Connection,
) : TextToSpeechProcessor, AutoCloseable {

    constructor(
        service: TextToSpeechService,
        topic: String = "TTS",
        group: String = "TTS",
        connectionUrl: String = "nats://localhost:4222",
    ) : this(service, topic, group, { Nats.connect(connectionUrl) })

    private val run = AtomicBoolean(false)
    private val connection by lazy {
        connectionFactory().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
        }
    }

    override suspend fun process(coroutineContext: CoroutineContext) {
        val dispatcher = connection.createDispatcher { msg: Message ->
            val requestId = msg.data.toString(Charsets.UTF_8)
            val body = try {
                if (!service.containsResource(requestId)) {
                    if (logger.isDebugEnabled) {
                        logger.debug("'{}' cannot be found.", requestId)
                    }
                    null
                } else {
                    service.getResource(requestId)
                }
            } catch (ex: Exception) {
                logger.error("TTS-lib: exception, request-id='{}'", requestId, ex)
                EXCEPTION_PREFIX + ex.stackTraceToString().toByteArray(Charsets.UTF_8)
            }
            if (logger.isTraceEnabled) {
                logger.trace("publish to {}; body-size={}", requestId, body?.size)
            }
            connection.publish(msg.replyTo, body)
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

    companion object {
        private val EXCEPTION_PREFIX = "e:".toByteArray(Charsets.UTF_8)
    }
}