package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.noUserFoundDbError

class PgDbUserRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
) : DbUserRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun getUser(authId: AppAuthId): UserEntityDbResponse {
        return connection.execute {
            if (PgDbDictionary.find { Dictionaries.userId eq authId.asString() }.empty()) {
                UserEntityDbResponse(
                    user = AppUserEntity.EMPTY, errors = listOf(noUserFoundDbError("getUser", authId))
                )
            } else {
                val res = AppUserEntity(authId = authId, id = AppUserId("42")) // TODO
                UserEntityDbResponse(user = res)
            }
        }
    }
}