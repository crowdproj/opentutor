package com.gitlab.sszuev.flashcards.common

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

    fun partsOfSpeech(lang: String): List<String> {
        return languages[lang.lowercase()]?: emptyList()
    }
}