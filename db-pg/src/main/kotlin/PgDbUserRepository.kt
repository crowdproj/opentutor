package com.gitlab.sszuev.flashcards.dbpg

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.sszuev.flashcards.common.noUserFoundDbError
import com.gitlab.sszuev.flashcards.common.wrongUserUUIDDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.User
import com.gitlab.sszuev.flashcards.dbpg.dao.Users
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class PgDbUserRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
) : DbUserRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }
    private val cache: Cache<UUID, User> = Caffeine.newBuilder().build()

    override fun getUser(authId: AppAuthId): UserEntityDbResponse {
        val uuid = try {
            UUID.fromString(authId.asString())
        } catch (ex: IllegalArgumentException) {
            return UserEntityDbResponse(
                user = AppUserEntity.EMPTY, errors = listOf(wrongUserUUIDDbError("getUser", authId))
            )
        }
        return connection.execute {
            // use local cache
            // expected that deletion when using different devices is rare
            // also, currently user has no details
            val user = cache.getIfPresent(uuid) ?: User.find(Users.uuid eq uuid).singleOrNull()
            if (user == null) {
                UserEntityDbResponse(
                    user = AppUserEntity.EMPTY, errors = listOf(noUserFoundDbError("getUser", authId))
                )
            } else {
                cache.put(uuid, user)
                UserEntityDbResponse(user = user.toEntity())
            }
        }
    }
}