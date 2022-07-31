package com.gitlab.sszuev.flashcards.speaker

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TTSClientSettings {
    private val logger = LoggerFactory.getLogger(TTSClientSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val requestTimeoutInMilliseconds = conf.get(key = "tts-client.request-timeout-in-milliseconds", default = 1000L)

    val host = conf.get(key = "tts-client.rabbitmq.host", default = "localhost")
    val port = conf.get(key = "tts-client.rabbitmq.port", default = 5672)
    val user = conf.get(key = "tts-client.rabbitmq.user", default = "dev")
    val password = conf.get(key = "tts-client.rabbitmq.password", default = "dev")

    val routingKeyRequest = conf.get("tts-client.rabbitmq.routing-key-request", default = "resource-identifier")
    val routingKeyResponsePrefix =
        conf.get("tts-client.rabbitmq.routing-key-response-prefix", default = "resource-body=")
    val consumerTag = conf.get("tts-client.rabbitmq.consumer-tag", default = "tts-server-consumer")
    val exchangeName = conf.get("tts-client.rabbitmq.exchange-name", default = "tts-exchange")
    val messageStatusHeader = conf.get("tts-client.rabbitmq.message-status-header", default = "status")

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

    private fun Config.get(key: String, default: String): String {
        return if (hasPath(key)) getString(key) else default
    }

    private fun Config.get(key: String, default: Int): Int {
        return if (hasPath(key)) getInt(key) else default
    }

    private fun Config.get(key: String, default: Long): Long {
        return if (hasPath(key)) getLong(key) else default
    }
}