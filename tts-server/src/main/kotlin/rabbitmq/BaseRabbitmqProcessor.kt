package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Delivery
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

private val logger = LoggerFactory.getLogger(BaseRabbitmqProcessor::class.java)

abstract class BaseRabbitmqProcessor(
    private val queueConfig: QueueConfig,
    private val createConnection: () -> Connection,
) : AutoCloseable {

    private val run = AtomicBoolean(false)

    /**
     * Runs the endless lifecycle, which processes messages.
     * @param [dispatcher][CoroutineScope]
     */
    suspend fun runLifecycle(dispatcher: CoroutineContext = Dispatchers.IO) {
        withContext(dispatcher) {
            createConnection().use { connection ->
                connection.createChannel().use { channel ->
                    channel.setupAndProcess()
                }
            }
        }
    }

    private fun Channel.setupAndProcess() {
        run.set(true)
        val onDeliver = deliverCallback(
            publishMessage = { t, m -> publishMessage(t, m) },
            onError = { t, m, ex -> handleError(t, m, ex) }
        )
        val onCancel = cancelCallback()
        runBlocking {
            exchange(exchangeName = queueConfig.exchangeName, exchangeType = "direct")
                .queue(queueName = queueConfig.requestQueueName)
                .bind(
                    queueName = queueConfig.requestQueueName,
                    exchangeName = queueConfig.exchangeName,
                    routingKey = queueConfig.routingKeyRequest
                )
                .consume(
                    queueName = queueConfig.requestQueueName,
                    consumerTag = queueConfig.consumerTag,
                    deliverCallback = onDeliver,
                    cancelCallback = onCancel,
                )
            while (run.get() && isOpen) {
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

    protected abstract suspend fun Channel.publishMessage(tag: String, message: Delivery)

    protected open fun Channel.handleError(tag: String, message: Delivery, ex: Throwable) {
        logger.error("Error while processing [$tag]", ex)
    }

    override fun close() {
        logger.info("Close.")
        run.set(false)
    }
}