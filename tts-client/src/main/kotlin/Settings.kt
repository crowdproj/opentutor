package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.config.BaseConfig
import org.slf4j.LoggerFactory

object Settings : BaseConfig("/application.properties") {
    private val logger = LoggerFactory.getLogger(Settings::class.java)

    val requestTimeoutInMilliseconds = getValue(key = "tts-client.request-timeout-in-milliseconds", default = 1000L)

    val host = getValue(key = "tts-client.rabbitmq.host", default = "localhost")
    val port = getValue(key = "tts-client.rabbitmq.port", default = 5672)
    val user = getValue(key = "tts-client.rabbitmq.user", default = "dev")
    val password = getValue(key = "tts-client.rabbitmq.password", default = "dev")

    val routingKeyIn = getValue("tts-client.rabbitmq.routing-key-in", default = "resource-identifier")
    val routingKeyOut = getValue("tts-client.rabbitmq.routing-key-out", default = "resource-body")
    val queueName = getValue("tts-client.rabbitmq.queue-name", default = "tts-queue")
    val consumerTag = getValue("tts-client.rabbitmq.consumer-tag", default = "tts-server-consumer")
    val exchangeName = getValue("tts-client.rabbitmq.exchange-name", default = "tts-exchange")
    val exchangeType = getValue("tts-client.rabbitmq.exchange-type", default = "direct")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |host           = $host
            |port           = $port
            |user           = ***
            |password       = ***            
            |routingKeyIn   = $routingKeyIn
            |routingKeyOut  = $routingKeyOut
            |queueName      = $queueName
            |consumerTag    = $consumerTag
            |exchangeName   = $exchangeName
            |exchangeType   = $exchangeType
            |timeout-in-ms  = $requestTimeoutInMilliseconds
            """.replaceIndentByMargin("\t")
    }
}