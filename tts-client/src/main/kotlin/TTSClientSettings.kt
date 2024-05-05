package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TTSClientSettings {
    private val logger = LoggerFactory.getLogger(TTSClientSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val requestTimeoutInMilliseconds = conf.get(key = "tts-client.request-timeout-in-milliseconds", default = 1000L)

    val host = conf.get(key = "tts-client.nats.host", default = "localhost")
    val port = conf.get(key = "tts-client.nats.port", default = 5672)
    val user = conf.get(key = "tts-client.nats.user", default = "dev")
    val password = conf.get(key = "tts-client.nats.password", default = "dev")
    val topic = conf.get(key = "tts-client.nats.topic", default = "TTS")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |nats-host                  = $host
            |nats-port                  = $port
            |nats-user                  = ***
            |nats-password              = ***            
            |nats-topic                 = $topic
            """.replaceIndentByMargin("\t")
    }
}