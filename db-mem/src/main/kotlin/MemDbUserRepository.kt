package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import kotlinx.datetime.Clock

class MemDbUserRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbUserRepository {

    private val database by lazy { MemDatabase.get(databaseLocation = dbConfig.dataLocation) }

    override fun findByUserId(id: String): DbUser? {
        return database.findUserById(id)?.toDbUser()
    }

    override fun createUser(user: DbUser): DbUser {
        val new = user.copy(createdAt = Clock.System.now())
        database.saveUser(new.toMemDbUser())
        return new
    }
}