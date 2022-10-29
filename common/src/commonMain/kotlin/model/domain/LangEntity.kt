package com.gitlab.sszuev.flashcards.model.domain

data class LangEntity(
    val langId: LangId = LangId.NONE,
    val partsOfSpeech: List<String> = emptyList(),
) {
    companion object {
        val EMPTY = LangEntity()
    }
}
