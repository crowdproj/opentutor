package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
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
        val uuid = try {
            UUID.fromString(authId.asString())
        } catch (ex: IllegalArgumentException) {
            return UserEntityDbResponse(
                user = AppUserEntity.EMPTY, errors = listOf(wrongUserUuidDbError("getUser", authId))
            )
        }
        val res = database.findUserByUuid(uuid)
            ?: return UserEntityDbResponse(
                user = AppUserEntity.EMPTY, errors = listOf(noUserFoundDbError("getUser", authId))
            )
        return UserEntityDbResponse(user = res.toAppUserEntity())
    }
}