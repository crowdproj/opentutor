package com.github.sszuev.flashcards.android.entities

data class CardEntity (
    val cardId: String,
    val dictionaryId: String,
    val word: String,
    val translation: String,
    val answered: Int
)