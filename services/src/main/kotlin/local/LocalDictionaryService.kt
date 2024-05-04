package com.gitlab.sszuev.flashcards.services.local

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.localAppRepositories

class LocalDictionaryService : DictionaryService {
    private val processor = DictionaryCorProcessor()

    override suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun createDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun uploadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        this.repositories = localAppRepositories
        processor.execute(this)
        return this
    }
}