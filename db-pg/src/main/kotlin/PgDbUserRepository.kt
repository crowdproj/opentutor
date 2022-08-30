package com.gitlab.sszuev.flashcards.dbpg

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.sszuev.flashcards.common.noUserFoundDbError
import com.gitlab.sszuev.flashcards.common.wrongUserUUIDDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.User
import com.gitlab.sszuev.flashcards.dbpg.dao.Users
import com.gitlab.sszuev.flashcards.model.domain.UserEntity
import com.gitlab.sszuev.flashcards.model.domain.UserUid
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

    override fun getUser(uid: UserUid): UserEntityDbResponse {
        val uuid = try {
            UUID.fromString(uid.asString())
        } catch (ex: IllegalArgumentException) {
            return UserEntityDbResponse(
                user = UserEntity.EMPTY, errors = listOf(wrongUserUUIDDbError("getUser", uid))
            )
        }
        return connection.execute {
            // use local cache
            // expected that deletion when using different devices is rare
            // also, currently user has no details
            val user = cache.getIfPresent(uuid) ?: User.find(Users.uuid eq uuid).singleOrNull()
            if (user == null) {
                UserEntityDbResponse(
                    user = UserEntity.EMPTY, errors = listOf(noUserFoundDbError("getUser", uid))
                )
            } else {
                cache.put(uuid, user)
                UserEntityDbResponse(user = user.toEntity())
            }
        }
    }
}