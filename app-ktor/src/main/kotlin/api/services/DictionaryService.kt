package com.gitlab.sszuev.flashcards.api.services

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor

class DictionaryService {

    private val processor = DictionaryCorProcessor()

    /**
     * Gets all dictionaries by user id.
     */
    suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()

    /**
     * Deletes the given dictionary.
     */
    suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    /**
     * Downloads dictionary.
     */
    suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    /**
     * Uploads dictionary.
     */
    suspend fun uploadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        processor.execute(this)
        return this
    }
}