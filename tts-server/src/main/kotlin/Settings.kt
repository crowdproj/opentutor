package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.config.BaseConfig
import org.slf4j.LoggerFactory

object Settings : BaseConfig("/application.properties") {
    private val logger = LoggerFactory.getLogger(Settings::class.java)

    val host = getValue(key = "tts-server.rabbitmq.host", default = "localhost")
    val port = getValue(key = "tts-server.rabbitmq.port", default = 5672)
    val user = getValue(key = "tts-server.rabbitmq.user", default = "dev")
    val password = getValue(key = "tts-server.rabbitmq.password", default = "dev")

    val routingKeyIn = getValue("tts-server.rabbitmq.routing-key-in", default = "resource-identifier")
    val routingKeyOut = getValue("tts-server.rabbitmq.routing-key-out", default = "resource-body")
    val queueName = getValue("tts-server.rabbitmq.queue-name", default = "tts-queue")
    val consumerTag = getValue("tts-server.rabbitmq.consumer-tag", default = "tts-server-consumer")
    val exchangeName = getValue("tts-server.rabbitmq.exchange-name", default = "tts-exchange")
    val exchangeType = getValue("tts-server.rabbitmq.exchange-type", default = "direct")

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
            """.replaceIndentByMargin("\t")
    }
}