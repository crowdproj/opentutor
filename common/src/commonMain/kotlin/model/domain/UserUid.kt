package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.Id

@JvmInline
value class UserUid(private val id: String) : Id {
    override fun asString() = id

    companion object {
        val NONE = UserUid("")
    }
}