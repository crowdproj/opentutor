package com.gitlab.sszuev.flashcards.model

import kotlinx.serialization.Serializable

interface Id {
    fun asString(): String

    @Serializable
    object NONE : Id {
        override fun asString(): String = ""
    }
}

