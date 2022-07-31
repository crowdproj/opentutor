package com.gitlab.sszuev.flashcards.dbmem

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object MemDbSettings {
    private val logger = LoggerFactory.getLogger(MemDbSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val dataLocation = conf.get(key = "db-mem.data-directory", default = "./db-data")
    val numberOfRightAnswers = conf.get(key = "app.tutor.run.answers", default = 10)

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |data-location                  = $dataLocation
            |number-of-right-answers        = $numberOfRightAnswers
            """.replaceIndentByMargin("\t")
    }

    private fun Config.get(key: String, default: String): String {
        return if (hasPath(key)) getString(key) else default
    }

    private fun Config.get(key: String, default: Int): Int {
        return if (hasPath(key)) getInt(key) else default
    }
}