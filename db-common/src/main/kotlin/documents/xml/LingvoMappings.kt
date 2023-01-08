package com.gitlab.sszuev.flashcards.common.documents.xml

import com.gitlab.sszuev.flashcards.common.documents.DocumentCardStatus
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal object LingvoMappings {
    /**
     * Impl notes:
     * Uses [StandardCharsets.UTF_16],
     * Lingvo ABBYY Tutor Words 16.1.3.70 requires UTF-16.
     */
    val charset: Charset = StandardCharsets.UTF_16

    private val LANGUAGE_MAP: Map<String, String> = mapOf(
        "1033" to "en",
        "1049" to "ru",
    )
    private val PART_OF_SPEECH_MAP = mapOf(
        "1" to "noun",
        "2" to "adjective",
        "3" to "verb",
    )
    private val STATUS_MAP = mapOf(
        "2" to DocumentCardStatus.UNKNOWN,
        "3" to DocumentCardStatus.IN_PROCESS,
        "4" to DocumentCardStatus.LEARNED
    )

    fun toLanguageTag(lingvoId: String): String {
        return byKey(LANGUAGE_MAP, lingvoId)
    }

    fun fromLanguageTag(tag: String): String {
        return byValue(LANGUAGE_MAP, tag)
    }

    fun toPartOfSpeechTag(lingvoId: String): String {
        return byKey(PART_OF_SPEECH_MAP, lingvoId)
    }

    fun fromPartOfSpeechTag(tag: String): String {
        return byValue(PART_OF_SPEECH_MAP, tag)
    }

    fun toStatus(id: String): DocumentCardStatus {
        return requireNotNull(STATUS_MAP[id])
    }

    fun fromStatus(status: DocumentCardStatus): String {
        return STATUS_MAP.entries.single { it.value == status }.key
    }

    private fun byValue(map: Map<String, String>, value: String): String {
        return map.entries.single { value.equals(it.value, ignoreCase = true) }.key
    }

    private fun byKey(map: Map<String, String>, key: String): String {
        return requireNotNull(map[key]) { "Can't find key '$key'" }
    }
}