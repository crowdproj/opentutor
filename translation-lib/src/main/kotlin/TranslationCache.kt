package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity

interface TranslationCache {
    fun get(sourceLang: String, targetLang: String, word: String): List<TranslationEntity>?

    fun put(sourceLang: String, targetLang: String, word: String, value: List<TranslationEntity>)
}