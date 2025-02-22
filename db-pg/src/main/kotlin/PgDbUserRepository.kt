package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.asJava
import com.gitlab.sszuev.flashcards.asKotlin
import com.gitlab.sszuev.flashcards.common.CommonUserDetailsDto
import com.gitlab.sszuev.flashcards.common.detailsAsCommonUserDetailsDto
import com.gitlab.sszuev.flashcards.common.parseUserDetailsJson
import com.gitlab.sszuev.flashcards.common.toJsonString
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbUser
import com.gitlab.sszuev.flashcards.dbpg.dao.Users
import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

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
            details = parseUserDetailsJson(user.details),
        )
    }

    override fun createUser(user: DbUser): DbUser = connection.execute {
        val now = Clock.System.now().truncateToMills()
        val detailsAsString = user.detailsAsCommonUserDetailsDto().toJsonString()
        Users.insert {
            it[id] = user.id
            it[createdAt] = now.asJava()
            it[details] = detailsAsString
        }
        DbUser(
            id = user.id,
            createdAt = now,
            details = user.details,
        )
    }

    override fun updateUser(user: DbUser): DbUser = connection.execute {
        require(user.id.isNotBlank())
        val res = Users.update({ Users.id eq user.id }) {
            it[details] = user.detailsAsCommonUserDetailsDto().toJsonString()
        } == 1
        check(res) {
            "Unable to update user ${user.id}"
        }
        user
    }

    override fun findOrCreateUser(id: String, details: Map<String, Any>, onCreate: () -> Unit): DbUser =
        connection.execute {
            val existingUser = Users.selectAll().where { Users.id eq id }
                    .forUpdate()
                    .singleOrNull()

            if (existingUser != null) {
                return@execute DbUser(
                    id = existingUser[Users.id].value,
                    createdAt = existingUser[Users.createdAt].asKotlin(),
                    details = parseUserDetailsJson(existingUser[Users.details])
                )
            }

            val now = Clock.System.now().truncateToMills()
            val detailsAsString = CommonUserDetailsDto(details.toMutableMap()).toJsonString()

            Users.insert {
                it[Users.id] = id
                it[createdAt] = now.asJava()
                it[Users.details] = detailsAsString
            }

            onCreate()

            return@execute DbUser(id = id, createdAt = now, details = details)
        }

    override fun addUserDetails(id: String, newDetails: Map<String, Any>): DbUser = connection.execute {
        val existingUser = Users.selectAll().where { Users.id eq id }
                .forUpdate()
                .singleOrNull() ?: throw IllegalStateException("User $id not found")

        val currentDetails = parseUserDetailsJson(existingUser[Users.details])
        val updatedDetails = currentDetails + newDetails

        val detailsAsString = CommonUserDetailsDto(updatedDetails.toMutableMap()).toJsonString()
        Users.update({ Users.id eq id }) {
            it[details] = detailsAsString
        }

        DbUser(
            id = existingUser[Users.id].value,
            createdAt = existingUser[Users.createdAt].asKotlin(),
            details = updatedDetails
        )
    }
}