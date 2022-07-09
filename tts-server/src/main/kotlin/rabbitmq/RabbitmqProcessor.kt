package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Delivery
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

private val logger = LoggerFactory.getLogger(RabbitmqProcessor::class.java)

abstract class RabbitmqProcessor(
    private val queueConfig: QueueConfig,
    private val createConnection: () -> Connection,
) : AutoCloseable {

    private val run = AtomicBoolean(true)

    /**
     * Runs the endless lifecycle, which processes messages.
     * @param [dispatcher][CoroutineScope]
     */
    suspend fun runLifecycle(dispatcher: CoroutineContext = Dispatchers.IO) {
        withContext(dispatcher) {
            createConnection().use {
                it.createChannel().use { channel ->
                    val onDeliver = channel.deliverCallback(
                        publishMessage = { t, m -> channel.publishMessage(t, m, queueConfig.routingKeyOut) },
                        onError = { t, m, ex -> channel.handleError(t, m, queueConfig.routingKeyOut, ex) }
                    )
                    runBlocking {
                        channel
                            .exchangeDeclare(queueConfig)
                            .queueDeclare(queueConfig)
                            .queueBind(queueConfig, queueConfig.routingKeyIn)
                            .basicConsume(queueConfig, onDeliver, cancelCallback())
                        while (run.get() && channel.isOpen) {
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
                        logger.info("Channel for [${queueConfig.consumerTag}] was closed.")
                    }
                }
            }
        }
    }

    protected abstract suspend fun Channel.publishMessage(tag: String, message: Delivery, targetRoutingKey: String)

    protected open fun Channel.handleError(tag: String, message: Delivery, targetRoutingKey: String, ex: Throwable) {
        logger.error("Error while processing [$tag]", ex)
    }

    override fun close() {
        logger.info("Close.")
        run.set(false)
    }
}