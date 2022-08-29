package com.gitlab.sszuev.flashcards.dbpg

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import java.util.concurrent.ConcurrentHashMap

class PgDbConnector(config: PgDbConfig) {

    private val databaseConfig: DatabaseConfig = DatabaseConfig { }

    private val dataSource: HikariDataSource = HikariConfig().let {
        it.jdbcUrl = config.jdbcUrl
        it.username = config.jdbcUser
        it.password = config.jdbcPasswd
        it.maximumPoolSize = config.hikariPoolSize
        it.keepaliveTime = config.hikariPoolKeepAliveTimeMs
        it.connectionInitSql = "SET timezone = 'UTC'; COMMIT; "
        HikariDataSource(it)
    }

    init {
        Liquibase(
            "/migrations/changelog.xml",
            ClassLoaderResourceAccessor(),
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(dataSource.connection))
        ).run {
            update(Contexts(), LabelExpression())
        }
    }

    private val connection: Database = Database.connect(
        datasource = dataSource,
        databaseConfig = databaseConfig,
    )

    companion object {
        private val connections = ConcurrentHashMap<PgDbConfig, Database>()

        /**
         * Connection pool.
         * @param [config][PgDbConfig]
         * @return [Database] - dedicated connection for the given configuration
         */
        fun connection(config: PgDbConfig): Database {
            return connections.computeIfAbsent(config) { PgDbConnector(config).connection }
        }
    }
}