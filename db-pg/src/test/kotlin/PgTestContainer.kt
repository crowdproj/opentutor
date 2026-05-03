package com.gitlab.sszuev.flashcards.dbpg

import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Duration

object PgTestContainer {
    private const val USER = "dev"
    private const val PWD = "dev"
    private const val SCHEMA = "flashcards"

    private val container by lazy {
        PostgreSQLContainer("postgres:16.9-alpine").apply {
            withUsername(USER)
            withPassword(PWD)
            withDatabaseName(SCHEMA)
            withStartupTimeout(Duration.ofSeconds(300L))
            start()
        }
    }

    val config: PgDbConfig by lazy { PgDbConfig(jdbcUrl = container.jdbcUrl) }
}
