package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.config.BaseConfig
import org.slf4j.LoggerFactory

object PgDbSettings : BaseConfig("/application.properties") {
    private val logger = LoggerFactory.getLogger(PgDbSettings::class.java)

    val jdbcUrl = getValue(key = "db-pg.url", default = "jdbc:postgresql://localhost:5432/flashcards")
    val jdbcUser = getValue(key = "db-pg.user", default = "dev")
    val jdbcPasswd = getValue(key = "db-pg.pwd", default = "dev")
    val hikariPoolSize = getValue(key = "db-pg.hikari-pool.pool-size", default = 8)
    val hikariPoolKeepAliveTimeMs = getValue(key = "db-pg.hikari-pool.keep-alive-time-ms", default = 150000L)

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
}