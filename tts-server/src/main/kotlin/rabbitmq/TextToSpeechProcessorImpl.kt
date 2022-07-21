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
    config: ProcessorConfig,
) : BaseRabbitmqProcessor(config, { ConnectionFactory().configure(connectionConfig).newConnection() }),
    TextToSpeechProcessor {

    override suspend fun Channel.publishMessage(tag: String, message: Delivery) {
        val requestId = message.properties.messageId
        val responseRoutingKey = config.routingKeyResponsePrefix + requestId
        if (logger.isDebugEnabled) {
            logger.debug("[$tag]::: got request with id=${requestId}")
        }
        val responseBody = service.getResource(requestId)
        val responseId = config.messageSuccessResponsePrefix + requestId

        if (logger.isDebugEnabled) {
            logger.debug("[$tag]::: send response with id=${responseId} to {$responseRoutingKey}.")
        }
        val props = AMQP.BasicProperties.Builder()
            .messageId(responseId)
            .headers(mapOf(config.messageStatusHeader to true))
            .build()
        basicPublish(config.exchangeName, responseRoutingKey, props, responseBody)
    }

    override fun Channel.handleError(tag: String, message: Delivery, ex: Throwable) {
        val requestId = message.properties.messageId
        logger.error("[$tag]::: got error for request with id={$requestId}", ex)
        val responseRoutingKey = config.routingKeyResponsePrefix + requestId

        val responseId = config.messageErrorResponsePrefix + requestId
        val responseBody = ex.toString().toByteArray(Charsets.UTF_8)

        if (logger.isDebugEnabled) {
            logger.debug("[$tag]::: send error response with id=${responseId} to {$responseRoutingKey}.")
        }
        val props = AMQP.BasicProperties.Builder()
            .messageId(responseId)
            .headers(mapOf(config.messageStatusHeader to false))
            .build()
        basicPublish(config.exchangeName, responseRoutingKey, props, responseBody)
    }

    override suspend fun process(dispatcher: CoroutineContext) {
        runLifecycle(dispatcher)
    }
}