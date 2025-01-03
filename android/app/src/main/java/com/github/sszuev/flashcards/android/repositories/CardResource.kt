package com.github.sszuev.flashcards.android.repositories

import com.github.sszuev.flashcards.android.utils.MapStringAnySerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class CardResource(
    val cardId: String,
    val dictionaryId: String? = null,
    val words: List<CardWordResource>? = null,
    val stats: Map<String, Long>? = null,
    @Serializable(with = MapStringAnySerializer::class)
    val details: Map<String, @Polymorphic Any>? = null,
    val answered: Int? = null,
)

@Serializable
data class CardWordResource(
    val word: String? = null,
    val transcription: String? = null,
    val partOfSpeech: String? = null,
    val translations: List<List<String>>? = null,
    val examples: List<CardWordExampleResource>? = null,
    val sound: String? = null,
    val primary: Boolean? = null
)

@Serializable
data class CardWordExampleResource(
    val example: String? = null,
    val translation: String? = null
)