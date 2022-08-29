package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.dbError
import com.gitlab.sszuev.flashcards.common.noUserFoundDbError
import com.gitlab.sszuev.flashcards.model.domain.UserEntity
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import java.util.*

class MemDbUserRepository(
    dbConfig: MemDbConfig = MemDbConfig()
) : DbUserRepository {

    private val users = UserStore.load(
        location = dbConfig.dataLocation,
        dbConfig = dbConfig,
    )

    override fun getUser(uid: String): UserEntityDbResponse {
        val uuid = try {
            UUID.fromString(uid)
        } catch (ex: IllegalArgumentException) {
            return UserEntityDbResponse(
                user = UserEntity.EMPTY,
                errors = listOf(
                    dbError(
                        operation = "getUser",
                        fieldName = uid,
                        details = "wrong uuid=<$uid.>",
                        exception = ex
                    )
                )
            )
        }
        val res = users[uuid]
            ?: return UserEntityDbResponse(
                user = UserEntity.EMPTY, errors = listOf(noUserFoundDbError("getUser", uid))
            )
        return UserEntityDbResponse(user = res.toEntity())
    }
}