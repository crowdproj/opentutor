package com.gitlab.sszuev.flashcards.repositories

object NoOpDbUserRepository : DbUserRepository {
    override fun findByUserId(id: String): DbUser = noOp()

    override fun createUser(user: DbUser): DbUser = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}