package com.gitlab.sszuev.flashcards.services.impl

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor
import com.gitlab.sszuev.flashcards.services.DictionaryService

class DictionaryServiceImpl : DictionaryService {

    private val processor = DictionaryCorProcessor()

    override suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()

    override suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    override suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        processor.execute(this)
        return this
    }
}