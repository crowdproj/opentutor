package com.gitlab.sszuev.flashcards.nats

import io.nats.client.Connection
import io.nats.client.Dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private val logger = LoggerFactory.getLogger(NatsServerProcessor::class.java)

class NatsServerProcessor(
    private val topic: String,
    private val group: String,
    private val connection: Connection,
    private val messageHandler: MessageHandler,
    parallelism: Int,
) : AutoCloseable {
    private val run = AtomicBoolean(false)
    private val dispatcherRef = AtomicReference<Dispatcher?>()

    private val scope = CoroutineScope(
        Dispatchers.IO.limitedParallelism(parallelism) + SupervisorJob()
    )

    fun ready() = run.get()

    fun process(): Job {
        try {
            subscribe()
        } catch (ex: Throwable) {
            logger.error("Can't subscribe", ex)
            throw ex
        }
        run.set(true)
        return scope.launch {
            while (run.get()) {
                delay(100)
            }
        }
    }

    fun subscribe() {
        dispatcherRef.getAndSet(null)?.let {
            logger.warn(">>> Forcing re-subscription to topic=$topic")
            it.unsubscribe(topic)
        }

        val dispatcher = connection.createDispatcher { msg ->
            scope.launch {
                messageHandler.handleMessage(connection, msg)
            }
        }
        dispatcher.subscribe(topic, group)
        dispatcherRef.set(dispatcher)

        logger.info(">>> Subscribed to topic=$topic group=$group")
    }

    override fun close() {
        logger.info("Shutting down NatsServerProcessor...")
        run.set(false)
        runCatching {
            dispatcherRef.getAndSet(null)?.unsubscribe(topic)
        }.onFailure {
            logger.error("Can't unsubscribe", it)
        }
        runCatching {
            connection.close()
        }.onFailure {
            logger.error("Can't close connection", it)
        }
        scope.cancel()
    }
}