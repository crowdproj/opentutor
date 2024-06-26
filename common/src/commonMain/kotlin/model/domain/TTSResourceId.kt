package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.Id
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class TTSResourceId(private val id: String) : Id {
    override fun asString() = id

    companion object {
        val NONE = TTSResourceId("")
    }
}