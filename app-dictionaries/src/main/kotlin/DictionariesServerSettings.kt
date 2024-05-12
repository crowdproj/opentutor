package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object DictionariesServerSettings {
    private val logger = LoggerFactory.getLogger(DictionariesServerSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val host = conf.get(key = "dictionaries-server.nats.host", default = "localhost")
    val port = conf.get(key = "dictionaries-server.nats.port", default = 4222)
    val user = conf.get(key = "dictionaries-server.nats.user", default = "dev")
    val password = conf.get(key = "dictionaries-server.nats.password", default = "dev")
    val topic = conf.get(key = "dictionaries-server.nats.topic", default = "TTS")
    val group = conf.get(key = "dictionaries-server.nats.group", default = "TTS")

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