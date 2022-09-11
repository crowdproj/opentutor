package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.DictionaryRepositories

interface DictionaryService {

    /**
     * Provides access to repositories configuration.
     */
    fun repositories(): DictionaryRepositories

    /**
     * Gets all dictionaries by user id.
     */
    suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext
}