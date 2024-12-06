package com.gitlab.sszuev.flashcards.translation.impl

data class TranslationConfig(
    val getResourceTimeoutMs: Long = TranslationSettings.getResourceTimeoutMs,
    val httpClientConnectTimeoutMs: Long = TranslationSettings.httpClientConnectTimeoutMs,
    val httpClientRequestTimeoutMs: Long = TranslationSettings.httpClientRequestTimeoutMs,
    val translationServiceLingueeApiUrl: String = TranslationSettings.translationServiceLingueeApiUrl,
)