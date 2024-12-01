package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository

class MockTranslationRepository(
    private val invokeFetch: (String, String, String) -> List<TranslationEntity> = { _, _, _ -> emptyList() },
) : TranslationRepository {

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): List<TranslationEntity> =
        invokeFetch(sourceLang, targetLang, word)
}