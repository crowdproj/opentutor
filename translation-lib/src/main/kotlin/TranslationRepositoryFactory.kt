package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository

fun createTranslationRepository(): TranslationRepository {
    val hasGoogleTranslationService = TranslationSettings::class.java.getResource("/google-key.json") != null
    val hasYandexTranslationService =
        TranslationSettings.translationServiceYandexKey.takeIf { it != "secret" }?.isNotBlank() == true
    return if (hasGoogleTranslationService && hasYandexTranslationService) {
        CombinedTranslationRepository(
            primaryTranslationRepository = YandexTranslationRepository(),
            secondaryTranslationRepository = GoogleTranslationRepository()
        )
    } else if (hasGoogleTranslationService) {
        GoogleTranslationRepository()
    } else if (hasYandexTranslationService) {
        YandexTranslationRepository()
    } else {
        LingueeTranslationRepository()
    }
}