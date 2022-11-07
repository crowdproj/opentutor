package com.gitlab.sszuev.flashcards.model.domain

data class DictionaryEntity(
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val name: String = "",
    val sourceLang: LangEntity = LangEntity.EMPTY,
    val targetLang: LangEntity = LangEntity.EMPTY,
    val totalCardsCount: Int = 0,
    val learnedCardsCount: Int = 0,
) {
    companion object {
        val EMPTY = DictionaryEntity()
    }
}