package com.gitlab.sszuev.flashcards.dbpg

class PgDbHealthRepository(dbConfig: PgDbConfig = PgDbConfig.DEFAULT) {
    private val connector by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbHealthConnector.connector(dbConfig)
    }

    fun ping() = try {
        connector.connection().use { connection ->
            connection.prepareStatement("SELECT 42").use { stmt ->
                stmt.executeQuery().next()
            }
        }
    } catch (_: Exception) {
        false
    }
}