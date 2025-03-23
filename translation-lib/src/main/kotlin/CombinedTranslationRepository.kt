package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository

class CombinedTranslationRepository(
    private val primaryTranslationRepository: TranslationRepository,
    private val secondaryTranslationRepository: TranslationRepository,
) : TranslationRepository {

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): List<TranslationEntity> {
        val error = IllegalStateException("Error while fetching translation data")
        val primary = try {
            primaryTranslationRepository.fetch(sourceLang, targetLang, word)
        } catch (e: Exception) {
            error.addSuppressed(e)
            emptyList()
        }
        if (primary.isNotEmpty()) {
            return primary
        }
        try {
            return secondaryTranslationRepository.fetch(sourceLang, targetLang, word)
        } catch (e: Exception) {
            error.addSuppressed(e)
        }
        if (error.suppressed.isNotEmpty()) {
            throw error
        }
        return emptyList()
    }
}