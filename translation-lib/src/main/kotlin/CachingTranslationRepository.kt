package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(CachingTranslationRepository::class.java)

class CachingTranslationRepository(
    private val translationRepository: TranslationRepository,
    private val cache: TranslationCache = CaffeineTranslationCache(),
) : TranslationRepository {
    override suspend fun fetch(
        sourceLang: String,
        targetLang: String,
        word: String
    ): List<TranslationEntity> {
        cache.get(sourceLang, targetLang, word)?.let {
            logger.debug("Found in cache: ${it.size} entries.")
            return it
        }
        val result = translationRepository.fetch(sourceLang, targetLang, word)
        if (result.isNotEmpty()) {
            cache.put(sourceLang, targetLang, word, result)
        }
        return result
    }
}

fun TranslationRepository.withCache(cache: TranslationCache): TranslationRepository =
    CachingTranslationRepository(this, cache)