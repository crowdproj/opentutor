package com.gitlab.sszuev.flashcards.model.domain

data class UserEntity(val id: UserId, val uid: UserUid) {
    companion object {
        val EMPTY = UserEntity(id = UserId.NONE, uid = UserUid.NONE)
    }
}