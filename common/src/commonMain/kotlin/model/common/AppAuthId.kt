package com.gitlab.sszuev.flashcards.model.common

import com.gitlab.sszuev.flashcards.model.Id

@JvmInline
value class AppAuthId(private val id: String) : Id {
    override fun asString() = id

    companion object {
        val NONE = AppAuthId("")
    }
}