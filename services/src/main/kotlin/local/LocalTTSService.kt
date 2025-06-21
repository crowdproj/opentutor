package com.gitlab.sszuev.flashcards.services.local

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.core.TTSCorProcessor
import com.gitlab.sszuev.flashcards.services.TTSService

class LocalTTSService : TTSService {
    private val processor = TTSCorProcessor()

    override suspend fun getResource(context: TTSContext): TTSContext = context.exec()

    private suspend fun TTSContext.exec(): TTSContext {
        this.repository = localTTSRepository
        processor.execute(this)
        return this
    }
}