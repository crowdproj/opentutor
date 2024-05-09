package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.core.TTSCorProcessor
import com.gitlab.sszuev.flashcards.services.TTSService
import com.gitlab.sszuev.flashcards.services.remoteTTSRepository

class RemoteTTSService : TTSService {
    private val processor = TTSCorProcessor()

    override suspend fun getResource(context: TTSContext): TTSContext = context.exec()

    private suspend fun TTSContext.exec(): TTSContext {
        this.repository = remoteTTSRepository
        processor.execute(this)
        return this
    }
}