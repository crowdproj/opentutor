package com.gitlab.sszuev.flashcards.translation.impl

import kotlinx.serialization.Serializable

@Serializable
data class YandexEntry(
    val def: List<YandexEntryDefinition>
)

@Serializable
data class YandexEntryDefinition(
    val text: String,
    val pos: String? = null,
    val ts: String? = null,
    val tr: List<YandexEntryTranslation>
)

@Serializable
data class YandexEntryTranslation(
    val text: String,
    val pos: String? = null,
    val syn: List<YandexEntrySynonym>? = null,
    val ex: List<YandexEntryExample>? = null
)

@Serializable
data class YandexEntrySynonym(
    val text: String,
    val pos: String? = null
)

@Serializable
data class YandexEntryExample(
    val text: String,
    val tr: List<YandexEntryExampleTranslation>
)

@Serializable
data class YandexEntryExampleTranslation(
    val text: String
)
