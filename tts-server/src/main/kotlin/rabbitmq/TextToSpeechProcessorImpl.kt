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
) : RabbitmqProcessor(queueConfig, { ConnectionFactory().configure(connectionConfig).newConnection() }), TextToSpeechProcessor {

    override suspend fun Channel.publishMessage(tag: String, message: Delivery, targetRoutingKey: String) {
        val requestId = message.properties.messageId
        if (logger.isDebugEnabled) {
            logger.info("[$tag]::: got request request with id=${requestId}")
        }
        val responseBody = service.getResource(requestId)
        val responseId = "response={$requestId}"

        val props = AMQP.BasicProperties.Builder().messageId(responseId).build()
        if (logger.isDebugEnabled) {
            logger.info("[$tag]::: send response responseId=${responseId} to {$targetRoutingKey}.")
        }
        basicPublish(queueConfig.exchangeName, targetRoutingKey, props, responseBody)
    }

    override fun Channel.handleError(tag: String, message: Delivery, targetRoutingKey: String, ex: Throwable) {
        logger.error("[$tag]::: error", ex)
        val requestId = message.properties.messageId

        val responseId = "error-response={$requestId}"
        val responseBody = ex.toString().toByteArray(Charsets.UTF_8)

        val props = AMQP.BasicProperties.Builder().messageId(responseId).build()
        basicPublish(queueConfig.exchangeName, targetRoutingKey, props, responseBody)
    }

    override suspend fun process(dispatcher: CoroutineContext) {
        runLifecycle(dispatcher)
    }
}