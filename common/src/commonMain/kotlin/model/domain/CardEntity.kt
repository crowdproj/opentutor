package com.gitlab.sszuev.flashcards.model.domain

data class CardEntity(
    val cardId: CardId = CardId.NONE,
    val dictionaryId: DictionaryId = DictionaryId.NONE,
    val word: String = "",
)