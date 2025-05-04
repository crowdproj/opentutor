package com.gitlab.sszuev.flashcards.speaker

data class TTSRedisConfig(
    val url: String = "redis://${TTSServerSettings.redisHost}:${TTSServerSettings.redisPort}/0",
)