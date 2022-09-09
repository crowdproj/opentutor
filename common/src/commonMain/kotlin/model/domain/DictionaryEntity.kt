package com.gitlab.sszuev.flashcards.model.domain

data class DictionaryEntity(
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val name: String = "",
    val sourceLangId: LangId = LangId.NONE,
    val targetLangId: LangId = LangId.NONE,
    val userId: UserId = UserId.NONE,
) {
    companion object {
        val EMPTY = DictionaryEntity()
    }
}