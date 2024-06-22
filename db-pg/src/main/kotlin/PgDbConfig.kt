package com.gitlab.sszuev.flashcards.dbpg

data class PgDbConfig(
    val jdbcUrl: String = PgDbSettings.jdbcUrl,
    val jdbcUser: String = PgDbSettings.jdbcUser,
    val jdbcPasswd: String = PgDbSettings.jdbcPasswd,
    val hikariPoolSize: Int = PgDbSettings.hikariPoolSize,
    val hikariPoolKeepAliveTimeMs: Long = PgDbSettings.hikariPoolKeepAliveTimeMs,
) {
    companion object {
        val DEFAULT = PgDbConfig()
    }
}