package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.TranslationContext

interface TranslationService {
    suspend fun fetchTranslation(context: TranslationContext): TranslationContext
}