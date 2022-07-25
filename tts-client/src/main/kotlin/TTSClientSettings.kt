package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.config.BaseConfig
import org.slf4j.LoggerFactory

object TTSClientSettings : BaseConfig("/application.properties") {
    private val logger = LoggerFactory.getLogger(TTSClientSettings::class.java)

    val requestTimeoutInMilliseconds = getValue(key = "tts-client.request-timeout-in-milliseconds", default = 1000L)

    val host = getValue(key = "tts-client.rabbitmq.host", default = "localhost")
    val port = getValue(key = "tts-client.rabbitmq.port", default = 5672)
    val user = getValue(key = "tts-client.rabbitmq.user", default = "dev")
    val password = getValue(key = "tts-client.rabbitmq.password", default = "dev")

    val routingKeyRequest = getValue("tts-client.rabbitmq.routing-key-request", default = "resource-identifier")
    val routingKeyResponsePrefix = getValue("tts-client.rabbitmq.routing-key-response-prefix", default = "resource-body=")
    val consumerTag = getValue("tts-client.rabbitmq.consumer-tag", default = "tts-server-consumer")
    val exchangeName = getValue("tts-client.rabbitmq.exchange-name", default = "tts-exchange")
    val messageStatusHeader = getValue("tts-client.rabbitmq.message-status-header", default = "status")

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
            |consumer-tag                   = $consumerTag
            |exchange-name                  = $exchangeName
            |request-timeout-in-ms          = $requestTimeoutInMilliseconds
            |message-status-header          = $messageStatusHeader
            """.replaceIndentByMargin("\t")
    }
}