package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserProvider
import com.gitlab.sszuev.flashcards.model.domain.UserEntity
import com.gitlab.sszuev.flashcards.model.domain.UserUid

interface DbUserRepository: AppUserProvider {
    fun getUser(uid: UserUid): UserEntityDbResponse
}

data class UserEntityDbResponse(val user: UserEntity, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = UserEntityDbResponse(user = UserEntity.EMPTY)
    }
}
