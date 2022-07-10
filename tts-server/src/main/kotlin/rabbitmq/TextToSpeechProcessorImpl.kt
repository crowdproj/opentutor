package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.controllers.TextToSpeechProcessor
import com.gitlab.sszuev.flashcards.speaker.services.TextToSpeechService
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Delivery
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

private val logger = LoggerFactory.getLogger(TextToSpeechProcessorImpl::class.java)

class TextToSpeechProcessorImpl(
    private val service: TextToSpeechService,
    connectionConfig: ConnectionConfig,
    private val queueConfig: QueueConfig,
) : BaseRabbitmqProcessor(queueConfig, { ConnectionFactory().configure(connectionConfig).newConnection() }),
    TextToSpeechProcessor {

    override suspend fun Channel.publishMessage(tag: String, message: Delivery) {
        val requestId = message.properties.messageId
        val responseRoutingKey = queueConfig.routingKeyResponsePrefix + requestId
        if (logger.isDebugEnabled) {
            logger.debug("[$tag]::: got request with id=${requestId}")
        }
        val responseBody = service.getResource(requestId)
        val responseId = "response-success={$requestId}"

        if (logger.isDebugEnabled) {
            logger.debug("[$tag]::: send response with id=${responseId} to {$responseRoutingKey}.")
        }
        val props = AMQP.BasicProperties.Builder().messageId(responseId).build()
        basicPublish(queueConfig.exchangeName, responseRoutingKey, props, responseBody)
    }

    override fun Channel.handleError(tag: String, message: Delivery, ex: Throwable) {
        val requestId = message.properties.messageId
        logger.error("[$tag]::: got error for request with id={$requestId}", ex)
        val responseRoutingKey = queueConfig.routingKeyResponsePrefix + requestId

        val responseId = "response-error={$requestId}"
        val responseBody = ex.toString().toByteArray(Charsets.UTF_8)

        if (logger.isDebugEnabled) {
            logger.debug("[$tag]::: send error response with id=${responseId} to {$responseRoutingKey}.")
        }
        val props = AMQP.BasicProperties.Builder().messageId(responseId).build()
        basicPublish(queueConfig.exchangeName, responseRoutingKey, props, responseBody)
    }

    override suspend fun process(dispatcher: CoroutineContext) {
        runLifecycle(dispatcher)
    }
}