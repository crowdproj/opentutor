package com.gitlab.sszuev.flashcards.speaker

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TTSSettings {
    private val logger = LoggerFactory.getLogger(TTSSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val localDataDirectory = conf.get("tts.local.data-directory", default = "classpath:/data")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |local-data-directory           = $localDataDirectory
            """.replaceIndentByMargin("\t")
    }

    private fun Config.get(key: String, default: String): String {
        return if (hasPath(key)) getString(key) else default
    }
}