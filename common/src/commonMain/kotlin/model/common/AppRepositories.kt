package com.gitlab.sszuev.flashcards.model.common

interface AppRepositories {
    fun userRepository(mode: AppMode): AppUserProvider
}

interface AppUserProvider