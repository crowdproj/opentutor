package com.gitlab.sszuev.flashcards.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class LangEntity(
    val langId: LangId = LangId.NONE,
    val partsOfSpeech: List<String> = emptyList(),
) {
    companion object {
        val EMPTY = LangEntity()
    }
}
