package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.nats.runApp
import com.gitlab.sszuev.flashcards.translation.impl.createTranslationRepository

suspend fun main() {
    val redisConfig = TranslationRedisConfig()
    val redis = TranslationRedisConnectionFactory(
        connectionUrl = redisConfig.url,
    )
    runApp(
        connectionUrl = "nats://${TranslationServerSettings.host}:${TranslationServerSettings.port}",
        topic = TranslationServerSettings.topic,
        group = TranslationServerSettings.group,
        parallelism = TranslationServerSettings.parallelism,
        messageHandler = TranslationMessageHandler(
            createTranslationRepository(
                cache = RedisTranslationCache(redis.stringToStringCommands),
            ),
        ),
        onShutdown = {
            redis.close()
        }
    )
}