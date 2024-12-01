package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TCard
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository

class MockTranslationRepository(
    private val invokeFetch: (String, String, String) -> TCard? = { _, _, _ -> null },
) : TranslationRepository {

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): TCard? =
        invokeFetch(sourceLang, targetLang, word)
}