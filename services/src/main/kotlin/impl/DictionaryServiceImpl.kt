package com.gitlab.sszuev.flashcards.services.impl

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor
import com.gitlab.sszuev.flashcards.services.DictionaryService

class DictionaryServiceImpl(
    private val repositories: DictionaryRepositories
) : DictionaryService {

    private val processor = DictionaryCorProcessor()

    override fun repositories(): DictionaryRepositories {
        return repositories
    }

    override suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        processor.execute(this)
        return this
    }
}