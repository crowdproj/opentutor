package com.gitlab.sszuev.flashcards.dbpg

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object PgDbSettings {
    private val logger = LoggerFactory.getLogger(PgDbSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val jdbcUrl = conf.get(key = "db-pg.url", default = "jdbc:postgresql://localhost:5432/flashcards")
    val jdbcUser = conf.get(key = "db-pg.user", default = "dev")
    val jdbcPasswd = conf.get(key = "db-pg.pwd", default = "dev")
    val hikariPoolSize = conf.get(key = "db-pg.hikari-pool.pool-size", default = 8)
    val hikariPoolKeepAliveTimeMs = conf.get(key = "db-pg.hikari-pool.keep-alive-time-ms", default = 150000L)

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |jdbc-connection-string         = $jdbcUrl
            |jdbc-connection-user           = ***
            |jdbc-connection-password       = ***
            |pool-size                      = $hikariPoolSize
            |keep-alive-time-ms             = $hikariPoolKeepAliveTimeMs
            """.replaceIndentByMargin("\t")
    }

    private fun Config.get(key: String, default: String): String {
        return if (hasPath(key)) getString(key) else default
    }

    private fun Config.get(key: String, default: Int): Int {
        return if (hasPath(key)) getInt(key) else default
    }

    private fun Config.get(key: String, default: Long): Long {
        return if (hasPath(key)) getLong(key) else default
    }
}