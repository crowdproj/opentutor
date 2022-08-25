package com.gitlab.sszuev.flashcards.dbmem

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object MemDbSettings {
    private val logger = LoggerFactory.getLogger(MemDbSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val dataLocation = conf.get(key = "db-mem.data-directory", default = "./db-data")
    val dataFlushPeriodInMs = conf.get(key = "db-mem.data-flush-period-in-ms", default = 1000L)

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |data-location                  = $dataLocation
            |data-flush-period-in-ms        = $dataFlushPeriodInMs
            """.replaceIndentByMargin("\t")
    }

    private fun Config.get(key: String, default: String): String {
        return if (hasPath(key)) getString(key) else default
    }

    private fun Config.get(key: String, default: Long): Long {
        return if (hasPath(key)) getLong(key) else default
    }

}