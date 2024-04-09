package com.gitlab.sszuev.flashcards.speaker

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.util.Locale

object ServerSettings {
    private val logger = LoggerFactory.getLogger(ServerSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val host = conf.get(key = "tts-server.rabbitmq.host", default = "localhost")
    val port = conf.get(key = "tts-server.rabbitmq.port", default = 5672)
    val user = conf.get(key = "tts-server.rabbitmq.user", default = "dev")
    val password = conf.get(key = "tts-server.rabbitmq.password", default = "dev")

    val routingKeyRequest = conf.get("tts-server.rabbitmq.routing-key-request", default = "resource-identifier")
    val routingKeyResponsePrefix =
        conf.get("tts-server.rabbitmq.routing-key-response-prefix", default = "resource-body=")
    val queueNameRequest = conf.get("tts-server.rabbitmq.queue-name-request", default = "tts-queue")
    val consumerTag = conf.get("tts-server.rabbitmq.consumer-tag", default = "tts-server-consumer")
    val exchangeName = conf.get("tts-server.rabbitmq.exchange-name", default = "tts-exchange")
    val messageSuccessResponsePrefix =
        conf.get("tts-server.rabbitmq.message-id-response-success-prefix", default = "response-success=")
    val messageErrorResponsePrefix =
        conf.get("tts-server.rabbitmq.message-id-response-error-prefix", default = "response-error=")
    val messageStatusHeader = conf.get("tts-server.rabbitmq.message-status-header", default = "status")

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

    private fun Config.get(key: String, default: String): String {
        val systemValue = getProperty(key)
        if (systemValue != null) {
            return systemValue
        }
        return if (hasPath(key)) getString(key) else default
    }

    private fun Config.get(key: String, default: Int): Int {
        val systemValue = getProperty(key)
        if (systemValue != null) {
            return systemValue.toInt()
        }
        return if (hasPath(key)) getInt(key) else default
    }

    private fun getProperty(key: String): String? {
        val systemPropertyValue = System.getProperty(key)
        if (systemPropertyValue != null) {
            return systemPropertyValue
        }
        val systemEnv1 = System.getenv(key)
        if (systemEnv1 != null) {
            return systemEnv1
        }
        val key2 = key.uppercase(Locale.US).replace(".", "_").replace("-", "_")
        val systemEnv2 = System.getenv(key2)
        if (systemEnv2 != null) {
            return systemEnv2
        }
        return null
    }
}