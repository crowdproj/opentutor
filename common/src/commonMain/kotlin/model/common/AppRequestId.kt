package com.gitlab.sszuev.flashcards.model.common

import com.gitlab.sszuev.flashcards.model.Id

@JvmInline
value class AppRequestId(private val id: String): Id {
    override fun asString() = id

    companion object {
        val NONE = AppRequestId("")
    }
}