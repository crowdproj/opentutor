package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppUserId

data class DictionaryEntity(
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val name: String = "",
    val sourceLangId: LangId = LangId.NONE,
    val targetLangId: LangId = LangId.NONE,
    val userId: AppUserId = AppUserId.NONE,
) {
    companion object {
        val EMPTY = DictionaryEntity()
    }
}