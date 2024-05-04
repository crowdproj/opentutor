package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.remoteAppRepositories

class RemoteDictionaryService : DictionaryService {
    private val processor = DictionaryCorProcessor()

    override suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun createDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun uploadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        this.repositories = remoteAppRepositories
        processor.execute(this)
        return this
    }
}