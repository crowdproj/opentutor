package com.gitlab.sszuev.flashcards.model.domain

data class TTSResourceGet(
    val word: String = "",
    val lang: LangId = LangId.NONE,
) {
    fun asResourceId() = TTSResourceId("${lang.asString()}:$word")

    companion object {
        val NONE = TTSResourceGet()
    }
}