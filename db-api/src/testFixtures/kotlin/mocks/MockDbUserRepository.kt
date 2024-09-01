package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.repositories.DbUser
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository

class MockDbUserRepository(
    private val invokeFindUserById: (String) -> DbUser? = { null },
    private val invokeCreateUser: (DbUser) -> DbUser = { it },
    private val invokeUpdateUser: (DbUser) -> DbUser = { it }
) : DbUserRepository {

    override fun findByUserId(id: String): DbUser? = invokeFindUserById(id)

    override fun createUser(user: DbUser): DbUser = invokeCreateUser(user)

    override fun updateUser(user: DbUser): DbUser = invokeUpdateUser(user)
}