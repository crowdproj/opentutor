package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.noUserFoundDbError
import com.gitlab.sszuev.flashcards.repositories.wrongUserUuidDbError
import java.util.UUID

class MemDbUserRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbUserRepository {

    private val database = MemDatabase.get(dbConfig.dataLocation)

    override fun getUser(authId: AppAuthId): UserEntityDbResponse {
        if (!database.containsUser(authId.asString())) {
            return UserEntityDbResponse(
                user = AppUserEntity.EMPTY, errors = listOf(noUserFoundDbError("getUser", authId))
            )
        }
        val res = AppUserEntity(authId = authId, id = AppUserId("42")) // TODO
        return UserEntityDbResponse(user = res)
    }
}