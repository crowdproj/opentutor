package com.gitlab.sszuev.flashcards.repositories

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface DbUserRepository {

    fun findByUserId(id: String): DbUser?

    fun createUser(user: DbUser): DbUser

    fun updateUser(user: DbUser): DbUser

    @ExperimentalTime
    fun findOrCreateUser(id: String, details: Map<String, Any> = emptyMap(), onCreate: () -> Unit = {}): DbUser {
        val found = findByUserId(id)
        if (found != null) {
            return found
        }
        val res = createUser(DbUser(id = id, details = details, createdAt = Clock.System.now()))
        onCreate()
        return res
    }

    @OptIn(ExperimentalTime::class)
    fun addUserDetails(id: String, newDetails: Map<String, Any>): DbUser {
        val found = findByUserId(id) ?: throw IllegalStateException()
        val mergedDetails = found.details + newDetails
        return updateUser(found.copy(details = mergedDetails))
    }
}