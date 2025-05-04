package com.gitlab.sszuev.flashcards.translation.impl

import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity

class CaffeineTranslationCache : TranslationCache {
    private val cache = Caffeine.newBuilder().maximumSize(1024)
        .build<Triple<String, String, String>, List<TranslationEntity>>()

    override fun get(
        sourceLang: String,
        targetLang: String,
        word: String
    ): List<TranslationEntity>? {
        return cache.getIfPresent(Triple(sourceLang, targetLang, word))
    }

    override fun put(
        sourceLang: String,
        targetLang: String,
        word: String,
        value: List<TranslationEntity>
    ) {
        cache.put(Triple(sourceLang, targetLang, word), value)
    }
}