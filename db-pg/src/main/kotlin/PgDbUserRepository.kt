package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.asJava
import com.gitlab.sszuev.flashcards.asKotlin
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbUser
import com.gitlab.sszuev.flashcards.dbpg.dao.Users
import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert

class PgDbUserRepository(
    dbConfig: PgDbConfig = PgDbConfig.DEFAULT,
) : DbUserRepository {

    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    // enforce connection
    fun connect() {
        connection
    }

    override fun findByUserId(id: String): DbUser? = connection.execute {
        val user = PgDbUser.findById(id) ?: return@execute null
        DbUser(
            id = user.id.value,
            createdAt = user.createdAt.asKotlin(),
        )
    }

    override fun createUser(user: DbUser): DbUser = connection.execute {
        val now = Clock.System.now()
        Users.insert {
            it[id] = user.id
            it[createdAt] = now.asJava()
        }
        DbUser(
            id = user.id,
            createdAt = now,
        )
    }
}