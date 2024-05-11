package com.gitlab.sszuev.flashcards.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class TTSResourceGet(
    val word: String = "",
    val lang: LangId = LangId.NONE,
) {
    fun asResourceId() = TTSResourceId("${lang.asString()}:$word")

    companion object {
        val NONE = TTSResourceGet()
    }
}