package com.gitlab.sszuev.flashcards.speaker

data class RedisConfig(
    val url: String = "redis://${TTSServerSettings.redisHost}:${TTSServerSettings.redisPort}",
)