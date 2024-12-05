package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object ServicesConfig {
    private val logger = LoggerFactory.getLogger(ServicesConfig::class.java)

    private val conf: Config = ConfigFactory.load()

    val requestTimeoutInMilliseconds = conf.get(key = "client.nats.request-timeout-in-ms", default = 5000L)
    val natsHost = conf.get(key = "client.nats.host", default = "localhost")
    val natsPort = conf.get(key = "client.nats.port", default = 4222)
    val natsUser = conf.get(key = "client.nats.user", default = "dev")
    val natsPassword = conf.get(key = "client.nats.password", default = "dev")
    val ttsNatsTopic = conf.get(key = "client.nats.topic.tts", default = "TTS")
    val translationNatsTopic = conf.get(key = "client.nats.topic.translation", default = "TRANSLATION")
    val cardsNatsTopic = conf.get(key = "client.nats.topic.cards", default = "CARDS")
    val dictionariesNatsTopic = conf.get(key = "client.nats.topic.dictionaries", default = "DICTIONARIES")
    val settingsNatsTopic = conf.get(key = "client.nats.topic.settings", default = "SETTINGS")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |nats-host                  = $natsHost
            |nats-port                  = $natsPort
            |nats-user                  = ***
            |nats-password              = ***            
            |nats-topic-tts             = $ttsNatsTopic
            |nats-topic-cards           = $cardsNatsTopic
            |nats-topic-dictionaries    = $dictionariesNatsTopic
            |nats-topic-settings        = $settingsNatsTopic
            |nats-topic-translation     = $translationNatsTopic
            |nats-request-timeout-ms    = $requestTimeoutInMilliseconds
            """.replaceIndentByMargin("\t")
    }
}