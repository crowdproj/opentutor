package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository

fun createTranslationRepository(): TranslationRepository =
    if (TranslationSettings::class.java.getResource("/google-key.json") != null) {
        GoogleTranslationRepository()
    } else {
        LingueeTranslationRepository()
    }