package com.gitlab.sszuev.flashcards.services.local

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.core.TranslationCorProcessor
import com.gitlab.sszuev.flashcards.services.TranslationService
import com.gitlab.sszuev.flashcards.services.localTranslationRepository

class LocalTranslationService : TranslationService {
    private val processor = TranslationCorProcessor()

    override suspend fun fetchTranslation(context: TranslationContext): TranslationContext = context.exec()

    private suspend fun TranslationContext.exec(): TranslationContext {
        this.repository = localTranslationRepository
        processor.execute(this)
        return this
    }
}