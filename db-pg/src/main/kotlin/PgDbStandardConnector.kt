package com.gitlab.sszuev.flashcards.dbpg

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.concurrent.ConcurrentHashMap

class PgDbStandardConnector(config: PgDbConfig) {

    private val databaseConfig: DatabaseConfig = DatabaseConfig { }

    private val dataSource: HikariDataSource = HikariConfig().let {
        it.jdbcUrl = config.jdbcUrl
        it.username = config.jdbcUser
        it.password = config.jdbcPasswd
        it.maximumPoolSize = config.hikariPoolSize
        it.keepaliveTime = config.hikariPoolKeepAliveTimeMs
        it.connectionInitSql = "SET timezone = 'UTC';"
        HikariDataSource(it)
    }

    init {
        dataSource.connection.use {
            Liquibase(
                "/migrations/changelog.xml",
                ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(it))
            ).run {
                update(Contexts(), LabelExpression())
            }
        }
    }

    val database: Database = Database.connect(
        datasource = dataSource,
        databaseConfig = databaseConfig,
    )

    companion object {
        private val connectors = ConcurrentHashMap<PgDbConfig, PgDbStandardConnector>()

        /**
         * Returns connector.
         * @param [config][PgDbConfig]
         * @return [PgDbStandardConnector] - dedicated connector for the given configuration
         */
        fun connector(config: PgDbConfig): PgDbStandardConnector {
            return connectors.computeIfAbsent(config) { PgDbStandardConnector(config) }
        }
    }
}