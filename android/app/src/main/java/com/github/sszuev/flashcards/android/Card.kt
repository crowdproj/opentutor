package com.github.sszuev.flashcards.android

data class Card (
    val cardId: String,
    val dictionaryId: String,
    val word: String,
    val translation: String,
    val answered: Int
)