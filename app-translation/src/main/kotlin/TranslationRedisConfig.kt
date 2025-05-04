package com.gitlab.sszuev.flashcards.translation

data class TranslationRedisConfig(
    val url: String = "redis://${TranslationServerSettings.redisHost}:${TranslationServerSettings.redisPort}/1",
)