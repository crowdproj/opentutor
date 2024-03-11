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
        "1025" to "ar",     // Arabic
        "1026" to "bg",     // Bulgarian
        "1028" to "ch",     // Chinese
        "1029" to "cs",     // Czech
        "1030" to "da",     // Danish
        "1031" to "de",     // German
        "1032" to "el",     // Greek
        "1034" to "es",     // Spanish (Traditional)
        "1035" to "fi",     // Finnish
        "1036" to "fr",     // French
        "1038" to "hu",     // Hungarian
        "1039" to "is",     // Icelandic
        "1040" to "it",     // Italian
        "1043" to "ni",     // Dutch
        "1044" to "no",     // Norwegian (Bokmal)
        "1045" to "pl",     // Polish
        "1048" to "ro",     // Romanian
        "1049" to "ru",     // Russian
        "1051" to "sk",     // Slovak
        "1053" to "sv",     // Swedish
        "1055" to "tr",     // Turkish
        "1057" to "in",     // Indonesian
        "1058" to "uk",     // Ukrainian
        "1059" to "be",     // Belarusian
        "1060" to "sl",     // Slovenian
        "1061" to "et",     // Estonian
        "1062" to "lv",     // Latvian
        "1063" to "lt",     // Lithuanian
        "1067" to "hy",     // Armenian
        "1069" to "eu",     // Basque
        "1078" to "af",     // Afrikaans
        "1079" to "ka",     // Georgian
        "1086" to "ms",     // Malay
        "1087" to "kk",     // Kazakh
        "1089" to "sw",     // Swahili
        "1092" to "tt",     // Tatar
        "1142" to "la",     // Latin
        "1561" to "ba",     // Bashkir
        "2052" to "ch",     // Chinese traditional
        "2068" to "no",     // Norwegian (Nynorsk)
        "2070" to "pt",     // Portuguese
        "3082" to "es",     // Spanish (Modern)
        "3098" to "sr",     // Serbian (Cyrillic)
        "32811" to "hy",    // Armenian Western
    )
    private val PART_OF_SPEECH_MAP = mapOf(
        "1" to "noun",
        "2" to "adjective",
        "3" to "verb",
        "4" to "phrasal",
        "5" to "phrasal verb",
        "6" to "adverb",
        "7" to "conjuction",
        "8" to "idiom",
        "9" to "numeral",
        "10" to "preposition",
        "11" to "pronoun",
        "12" to "question word",
    )
    private val STATUS_MAP = mapOf(
        "2" to DocumentCardStatus.UNKNOWN,
        "3" to DocumentCardStatus.IN_PROCESS,
        "4" to DocumentCardStatus.LEARNED,
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