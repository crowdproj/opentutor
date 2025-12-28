package com.github.sszuev.flashcards.android.entities

data class CardEntity (
    val cardId: String?,
    val dictionaryId: String?,
    val word: String,
    val translation: List<String>,
    val answered: Int,
    val examples: List<Pair<String, String?>>,
    val audioId: String,
)