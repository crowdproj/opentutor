package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.*

interface DbUserRepository : AppUserRepository {
    override fun getUser(authId: AppAuthId): UserEntityDbResponse
}

data class UserEntityDbResponse(
    override val user: AppUserEntity,
    override val errors: List<AppError> = emptyList()
) : AppUserResponse {
    companion object {
        val EMPTY = UserEntityDbResponse(user = AppUserEntity.EMPTY)
    }
}
