package com.gitlab.sszuev.flashcards.dbpg

import org.testcontainers.containers.PostgreSQLContainer
import java.time.Duration

object PgTestContainer {
    private const val user = "dev"
    private const val pwd = "dev"
    private const val schema = "flashcards"

    private class Container : PostgreSQLContainer<Container>("postgres:14.4")

    private val container by lazy {
        Container().apply {
            withUsername(user)
            withPassword(pwd)
            withDatabaseName(schema)
            withStartupTimeout(Duration.ofSeconds(300L))
            start()
        }
    }

    val config: PgDbConfig by lazy { PgDbConfig(jdbcUrl = container.jdbcUrl) }
}