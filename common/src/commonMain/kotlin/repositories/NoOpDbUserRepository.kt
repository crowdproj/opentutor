package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppAuthId

object NoOpDbUserRepository: DbUserRepository {
    override fun getUser(authId: AppAuthId): UserEntityDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}