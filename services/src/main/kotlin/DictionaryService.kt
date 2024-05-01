package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor

class DictionaryService {

    private val processor = DictionaryCorProcessor()

    suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()

    suspend fun createDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    suspend fun uploadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        processor.execute(this)
        return this
    }
}