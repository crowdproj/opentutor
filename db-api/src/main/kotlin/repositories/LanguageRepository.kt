package com.gitlab.sszuev.flashcards.repositories

import java.util.Locale
import java.util.Objects
import java.util.Properties

object LanguageRepository {
    private val languages = loadLanguages()

    private fun loadLanguages(): Map<String, List<String>> {
        val props = Properties()
        Objects.requireNonNull(LanguageRepository::class.java.getResourceAsStream("/parts-of-speech.props")).use {
            props.load(it.bufferedReader(Charsets.UTF_8))
        }
        val tags = Locale.getAvailableLocales().map { it.language }
        return tags.associateWith { tag ->
            props.getProperty(tag)?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        }
    }

    /**
     * Parts of speech by language tag.
     */
    fun partsOfSpeech(lang: String): List<String> = languages[lang.lowercase()] ?: emptyList()

    /**
     * Language tag to language name.
     */
    fun languages(): Map<String, String> = Locale.getAvailableLocales()
        .filterNot { it.language.isBlank() }
        .associate {
            if (it.language == "en") {
                it.language to it.getDisplayLanguage(Locale.US)
            } else {
                it.language to "${it.getDisplayLanguage(Locale.US)} (${it.getDisplayLanguage(it)})"
            }
        }
}