package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TranslationServerSettings {
    private val logger = LoggerFactory.getLogger(TranslationServerSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val host = conf.get(key = "translation-server.nats.host", default = "localhost")
    val port = conf.get(key = "translation-server.nats.port", default = 4222)
    val user = conf.get(key = "translation-server.nats.user", default = "dev")
    val password = conf.get(key = "translation-server.nats.password", default = "dev")
    val topic = conf.get(key = "translation-server.nats.topic", default = "SETTINGS")
    val group = conf.get(key = "translation-server.nats.group", default = "SETTINGS")

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