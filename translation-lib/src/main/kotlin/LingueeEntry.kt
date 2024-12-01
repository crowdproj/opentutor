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
    @SerialName("audio_links") val audioLinks: List<LingueeAudioLink>?,
    val translations: List<LingueeTranslation>
)

@Serializable
data class LingueeAudioLink(
    val url: String?,
    val lang: String?,
)

@Serializable
data class LingueeTranslation(
    val featured: Boolean,
    val text: String?,
    val pos: String?,
    @SerialName("audio_links") val audioLinks: List<LingueeAudioLink>?,
    val examples: List<LingueeExample>?,
    @SerialName("usage_frequency") val usageFrequency: String?
)

@Serializable
data class LingueeExample(
    val src: String,
    val dst: String
)