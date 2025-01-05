package com.github.sszuev.flashcards.android.entities

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryEntity(
    val dictionaryId: String?,
    val name: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val totalWords: Int,
    val learnedWords: Int,
    val numberOfRightAnswers: Int,
)