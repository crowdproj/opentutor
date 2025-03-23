package com.gitlab.sszuev.flashcards.translation.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LingueeEntry(
    val featured: Boolean,
    val text: String,
    val pos: String?,
    val forms: List<String>?,
    @SerialName("grammar_info") val grammarInfo: String?,
    @SerialName("audio_links") val audioLinks: List<LingueeEntryAudioLink>?,
    val translations: List<LingueeEntryTranslation>
)

@Serializable
data class LingueeEntryAudioLink(
    val url: String?,
    val lang: String?,
)

@Serializable
data class LingueeEntryTranslation(
    val featured: Boolean,
    val text: String?,
    val pos: String?,
    @SerialName("audio_links") val audioLinks: List<LingueeEntryAudioLink>?,
    val examples: List<LingueeEntryExample>?,
    @SerialName("usage_frequency") val usageFrequency: String?
)

@Serializable
data class LingueeEntryExample(
    val src: String,
    val dst: String
)