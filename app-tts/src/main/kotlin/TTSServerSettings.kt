package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TTSServerSettings {
    private val logger = LoggerFactory.getLogger(TTSServerSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val host = conf.get(key = "tts-server.nats.host", default = "localhost")
    val port = conf.get(key = "tts-server.nats.port", default = 4222)
    val user = conf.get(key = "tts-server.nats.user", default = "dev")
    val password = conf.get(key = "tts-server.nats.password", default = "dev")
    val topic = conf.get(key = "tts-server.nats.topic", default = "TTS")
    val group = conf.get(key = "tts-server.nats.group", default = "TTS")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |nats-host                      = $host
            |nats-port                      = $port
            |nats-user                      = ***
            |nats-password                  = ***           
            |nats-topic                     = $topic
            |nats-group                     = $group
            """.replaceIndentByMargin("\t")
    }
}