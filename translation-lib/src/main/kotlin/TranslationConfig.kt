package com.gitlab.sszuev.flashcards.translation.impl

data class TranslationConfig(
    val getResourceTimeoutMs: Long = TranslationSettings.getResourceTimeoutMs,
    val httpClientConnectTimeoutMs: Long = TranslationSettings.httpClientConnectTimeoutMs,
    val httpClientRequestTimeoutMs: Long = TranslationSettings.httpClientRequestTimeoutMs,
    val translationServiceLingueeApiHost: String = TranslationSettings.translationServiceLingueeApiHost,
    val translationServiceLingueeApiPath: String = TranslationSettings.translationServiceLingueeApiPath,
)