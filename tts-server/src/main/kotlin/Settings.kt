package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.config.BaseConfig
import org.slf4j.LoggerFactory

object Settings : BaseConfig("/application.properties") {
    private val logger = LoggerFactory.getLogger(Settings::class.java)

    val host = getValue(key = "tts-server.rabbitmq.host", default = "localhost")
    val port = getValue(key = "tts-server.rabbitmq.port", default = 5672)
    val user = getValue(key = "tts-server.rabbitmq.user", default = "dev")
    val password = getValue(key = "tts-server.rabbitmq.password", default = "dev")

    val routingKeyRequest = getValue("tts-server.rabbitmq.routing-key-request", default = "resource-identifier")
    val routingKeyResponsePrefix =
        getValue("tts-server.rabbitmq.routing-key-response-prefix", default = "resource-body=")
    val queueNameRequest = getValue("tts-server.rabbitmq.queue-name-request", default = "tts-queue")
    val consumerTag = getValue("tts-server.rabbitmq.consumer-tag", default = "tts-server-consumer")
    val exchangeName = getValue("tts-server.rabbitmq.exchange-name", default = "tts-exchange")
    val messageSuccessResponsePrefix =
        getValue("tts-server.rabbitmq.message-id-response-success-prefix", default = "response-success=")
    val messageErrorResponsePrefix =
        getValue("tts-server.rabbitmq.message-id-response-error-prefix", default = "response-error=")
    val messageStatusHeader = getValue("tts-server.rabbitmq.message-status-header", default = "status")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |rabbitmq-host                  = $host
            |rabbitmq-port                  = $port
            |rabbitmq-user                  = ***
            |rabbitmq-password              = ***            
            |routing-key-request            = $routingKeyRequest
            |routing-key-response-prefix    = $routingKeyResponsePrefix
            |queue-name-request             = $queueNameRequest
            |consumer-tag                   = $consumerTag
            |exchangeName                   = $exchangeName
            |message-success-prefix         = $messageSuccessResponsePrefix  
            |message-error-prefix           = $messageErrorResponsePrefix
            |message-status-header          = $messageStatusHeader
            """.replaceIndentByMargin("\t")
    }
}