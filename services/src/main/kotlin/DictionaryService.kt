package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DictionaryContext

interface DictionaryService {

    /**
     * Gets all dictionaries by user id.
     */
    suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext
}