package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryEntity(
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val userId: AppAuthId = AppAuthId.NONE,
    val name: String = "",
    val sourceLang: LangEntity = LangEntity.EMPTY,
    val targetLang: LangEntity = LangEntity.EMPTY,
    val totalCardsCount: Int = 0,
    val learnedCardsCount: Int = 0,
    val numberOfRightAnswers: Int = 10,
) {
    companion object {
        val EMPTY = DictionaryEntity()
    }
}