package com.gitlab.sszuev.flashcards.dbmem.documents.impl

import com.gitlab.sszuev.flashcards.common.CardStatus
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object LingvoMappings {
    /**
     * Impl notes:
     * Uses [StandardCharsets.UTF_16],
     * Lingvo ABBYY Tutor Words 16.1.3.70 requires UTF-16.
     */
    val charset: Charset = StandardCharsets.UTF_16

    private val LANGUAGE_MAP: Map<String, StandardLanguage> = mapOf(
        "1033" to StandardLanguage.EN,
        "1049" to StandardLanguage.RU
    )
    private val PART_OF_SPEECH_MAP = mapOf(
        "1" to StandardPartOfSpeech.NOUN,
        "2" to StandardPartOfSpeech.ADJECTIVE,
        "3" to StandardPartOfSpeech.VERB
    )
    private val STATUS_MAP = mapOf(
        "2" to CardStatus.UNKNOWN,
        "3" to CardStatus.IN_PROCESS,
        "4" to CardStatus.LEARNED
    )

    fun toLanguageTag(id: String): String {
        return byKey(LANGUAGE_MAP, id).name
    }

    fun fromLanguageTag(tag: String): String {
        return byValue(LANGUAGE_MAP, tag)
    }

    fun toPartOfSpeechTag(id: String): String {
        return byKey(PART_OF_SPEECH_MAP, id).name
    }

    fun fromPartOfSpeechTag(tag: String): String {
        return byValue(PART_OF_SPEECH_MAP, tag)
    }

    fun toStatus(id: String): CardStatus {
        return byKey(STATUS_MAP, id)
    }

    fun fromStatus(status: CardStatus): String {
        return byValue(STATUS_MAP, status.name)
    }

    private fun <X : Enum<X>?> byValue(map: Map<String, X>, value: String): String {
        return map.entries.asSequence().filter {
            value.equals(it.value!!.name, ignoreCase = true)
        }.single().key
    }

    private fun <X : Enum<X>?> byKey(map: Map<String, X>, key: String): X {
        return requireNotNull(map[key]) { "Can't find key '$key'" }
    }
}