package com.gitlab.sszuev.flashcards.model.domain

data class UserEntity(val id: UserId, val uid: String) {
    companion object {
        val EMPTY = UserEntity(id = UserId.NONE, uid = "")
    }
}