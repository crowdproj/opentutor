package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity

interface AppUserRepository {
    fun getUser(authId: AppAuthId): AppUserResponse
}

interface AppUserResponse {
    val user: AppUserEntity
    val errors: List<AppError>
}