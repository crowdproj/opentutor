package com.gitlab.sszuev.flashcards.translation.impl

data class TranslationConfig(
    val httpClientConnectTimeoutMs: Long = TranslationSettings.httpClientConnectTimeoutMs,
    val httpClientRequestTimeoutMs: Long = TranslationSettings.httpClientRequestTimeoutMs,
    val translationServiceYandexApi: String = TranslationSettings.translationServiceYandexApi,
    val translationServiceYandexKey: String = TranslationSettings.translationServiceYandexKey
)