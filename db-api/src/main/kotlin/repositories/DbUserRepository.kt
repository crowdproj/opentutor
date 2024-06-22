package com.gitlab.sszuev.flashcards.repositories

interface DbUserRepository {

    fun findByUserId(id: String): DbUser?

    fun createUser(user: DbUser): DbUser
}