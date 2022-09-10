package com.gitlab.sszuev.flashcards.model.common

interface AppRepositories {
    fun userRepository(mode: AppMode): AppUserRepository
}

interface AppUserRepository {
    fun getUser(authId: AppAuthId): AppUserResponse
}

interface AppUserResponse {
    val user: AppUserEntity
    val errors: List<AppError>
}