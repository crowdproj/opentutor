package com.gitlab.sszuev.flashcards.settings

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object SettingsServerSettings {
    private val logger = LoggerFactory.getLogger(SettingsServerSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val host = conf.get(key = "settings-server.nats.host", default = "localhost")
    val port = conf.get(key = "settings-server.nats.port", default = 4222)
    val user = conf.get(key = "settings-server.nats.user", default = "dev")
    val password = conf.get(key = "settings-server.nats.password", default = "dev")
    val topic = conf.get(key = "settings-server.nats.topic", default = "SETTINGS")
    val group = conf.get(key = "settings-server.nats.group", default = "SETTINGS")
    val parallelism = conf.get(key = "settings-server.nats.parallelism", default = 3)

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
            |parallelism                    = $parallelism
            """.replaceIndentByMargin("\t")
    }
}