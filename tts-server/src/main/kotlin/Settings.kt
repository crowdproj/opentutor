package com.gitlab.sszuev.flashcards.speaker

import org.slf4j.LoggerFactory
import java.util.*

object Settings {
    private val logger = LoggerFactory.getLogger(Settings::class.java)

    private val properties: Properties by lazy {
        loadProperties()
    }

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

    private fun loadProperties(): Properties {
        val res = Properties()
        val stream = Settings::class.java.getResourceAsStream("/application.properties")
        if (stream == null) {
            logger.warn("Can't find application.properties")
            return res
        }
        stream.use {
            res.load(it)
        }
        return res
    }

    private inline fun <reified X : Any> getValue(key: String, default: X): X {
        var res = System.getProperty(key)
        if (res != null) {
            return toTypedValue(res, default)
        }
        res = System.getenv(key)
        if (res != null) {
            return toTypedValue(res, default)
        }
        res = properties.getProperty(key)
        if (res != null) {
            return toTypedValue(res, default)
        }
        return default
    }

    private inline fun <reified X : Any> toTypedValue(value: String, typed: X): X {
        if (typed is String) {
            return value as X
        }
        if (typed is Int) {
            return value.toInt() as X
        }
        throw IllegalStateException("Wrong type: ${typed::class.simpleName}")
    }
}