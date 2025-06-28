package com.gitlab.sszuev.flashcards.dbpg

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

class PgDbHealthConnector(config: PgDbConfig) {

    private val dataSource: HikariDataSource = HikariConfig().let {
        it.jdbcUrl = config.jdbcUrl
        it.username = config.jdbcUser
        it.password = config.jdbcPasswd

        it.maximumPoolSize = 1
        it.minimumIdle = 0
        it.idleTimeout = 1000
        it.connectionTimeout = 1000
        it.validationTimeout = 500
        it.initializationFailTimeout = 0
        it.isAutoCommit = true
        it.connectionTestQuery = "SELECT 42"

        it.keepaliveTime = 0
        it.connectionInitSql = "SET timezone = 'UTC';"
        HikariDataSource(it)
    }

    fun connection(): Connection = dataSource.connection

    companion object {
        private val connectors = ConcurrentHashMap<PgDbConfig, PgDbHealthConnector>()

        /**
         * Returns connector.
         * @param [config][PgDbConfig]
         * @return [PgDbHealthConnector] - dedicated connector for the given configuration
         */
        fun connector(config: PgDbConfig): PgDbHealthConnector {
            return connectors.computeIfAbsent(config) { PgDbHealthConnector(config) }
        }
    }
}