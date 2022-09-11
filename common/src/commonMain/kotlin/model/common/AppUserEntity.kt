package com.gitlab.sszuev.flashcards.model.common

data class AppUserEntity(val id: AppUserId, val authId: AppAuthId) {
    companion object {
        val EMPTY = AppUserEntity(id = AppUserId.NONE, authId = AppAuthId.NONE)
    }
}