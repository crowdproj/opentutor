package com.gitlab.sszuev.flashcards.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class DocumentEntity(
    val name: String = "",
    val sourceLang: LangEntity = LangEntity.EMPTY,
    val targetLang: LangEntity = LangEntity.EMPTY,
    val cards: List<CardEntity> = emptyList(),
)