package com.gitlab.sszuev.flashcards.model.domain

data class ResourceGet(
    val word: String = "",
    val lang: LangId = LangId.NONE,
) {
    companion object {
        val NONE = ResourceGet()
    }
}