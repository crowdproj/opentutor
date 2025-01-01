package com.github.sszuev.flashcards.android.repositories

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryResource(
    val dictionaryId: String? = null,

    val name: String? = null,

    val sourceLang: String? = null,

    val targetLang: String? = null,

    val partsOfSpeech: List<String>? = null,

    val total: Int? = null,

    val learned: Int? = null,

    val numberOfRightAnswers: Int? = null
)
