package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.UserEntity

interface DbUserRepository {
    fun getUser(uid: String): UserEntityDbResponse
}

data class UserEntityDbResponse(val user: UserEntity, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = UserEntityDbResponse(user = UserEntity.EMPTY)
    }
}
