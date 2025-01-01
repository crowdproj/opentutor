package com.github.sszuev.flashcards.android

import kotlinx.serialization.Serializable

@Serializable
data class Dictionary(
    val name: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val totalWords: Int,
    val learnedWords: Int
)

val sampleData = listOf(
    Dictionary(
        name = "Irregular Verbs",
        sourceLanguage = "English",
        targetLanguage = "Russian (русский)",
        totalWords = 244,
        learnedWords = 0
    ),
    Dictionary(
        name = "Weather",
        sourceLanguage = "English",
        targetLanguage = "Russian (русский)",
        totalWords = 66,
        learnedWords = 42
    )
)
