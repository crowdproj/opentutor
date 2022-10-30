package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppUserId

data class DictionaryEntity(
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val name: String = "",
    val sourceLang: LangEntity = LangEntity.EMPTY,
    val targetLang: LangEntity = LangEntity.EMPTY,
    val userId: AppUserId = AppUserId.NONE,
    val totalCardsCount: Int = 0,
    val learnedCardsCount: Int = 0,
) {
    companion object {
        val EMPTY = DictionaryEntity()
    }
}