package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DictionaryContext

interface DictionaryService {

    /**
     * Gets all dictionaries by user id.
     */
    suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext

    /**
     * Deletes the given dictionary.
     */
    suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext

    /**
     * Download dictionary.
     */
    suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext
}