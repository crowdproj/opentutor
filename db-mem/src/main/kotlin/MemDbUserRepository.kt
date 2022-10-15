package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.noUserFoundDbError
import com.gitlab.sszuev.flashcards.common.wrongUserUUIDDbError
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import java.util.*

class MemDbUserRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    ids: IdSequences = IdSequences.globalIdsGenerator,
) : DbUserRepository {

    private val users = UserStore.load(
        location = dbConfig.dataLocation,
        dbConfig = dbConfig,
        ids = ids,
    )

    override fun getUser(authId: AppAuthId): UserEntityDbResponse {
        val uuid = try {
            UUID.fromString(authId.asString())
        } catch (ex: IllegalArgumentException) {
            return UserEntityDbResponse(
                user = AppUserEntity.EMPTY, errors = listOf(wrongUserUUIDDbError("getUser", authId))
            )
        }
        val res = users[uuid]
            ?: return UserEntityDbResponse(
                user = AppUserEntity.EMPTY, errors = listOf(noUserFoundDbError("getUser", authId))
            )
        return UserEntityDbResponse(user = res.toEntity())
    }
}