package com.gitlab.sszuev.flashcards.translation.api

/**
 * Represents a repository for fetching translation data.
 * Provides a mechanism to retrieve translations for words between specified languages.
 */
interface TranslationRepository {

    /**
     * Fetches translation data for a given word from the source language to the target language.
     *
     * @param sourceLang the language code of the source language
     * @param targetLang the language code of the target language
     * @param word the word to be translated
     * @return a TCard object containing translation data for the word
     */
    suspend fun fetch(sourceLang: String, targetLang: String, word: String): TCard?
}