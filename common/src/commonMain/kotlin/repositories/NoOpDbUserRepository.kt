package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.UserUid

object NoOpDbUserRepository: DbUserRepository {
    override fun getUser(uid: UserUid): UserEntityDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}