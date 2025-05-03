package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.NoOpTranslationRepository
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository

fun createTranslationRepository(
    cache: TranslationCache = CaffeineTranslationCache(),
): TranslationRepository {
    val hasGoogleTranslationService = TranslationSettings::class.java.getResource("/google-key.json") != null
    val hasYandexTranslationService =
        TranslationSettings.translationServiceYandexKey.takeIf { it != "secret" }?.isNotBlank() == true
    return if (hasGoogleTranslationService && hasYandexTranslationService) {
        CombinedTranslationRepository(
            primaryTranslationRepository = YandexTranslationRepository(),
            secondaryTranslationRepository = GoogleTranslationRepository()
        ).withCache(cache)
    } else if (hasGoogleTranslationService) {
        GoogleTranslationRepository().withCache(cache)
    } else if (hasYandexTranslationService) {
        YandexTranslationRepository().withCache(cache)
    } else {
        NoOpTranslationRepository
    }
}