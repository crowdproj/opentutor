package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.nats.runProcessing

suspend fun main() {
    val redisConfig = TTSRedisConfig()
    val redis = TTSRedisConnectionFactory(
        connectionUrl = redisConfig.url,
    )
    runProcessing(
        connectionUrl = "nats://${TTSServerSettings.natsHost}:${TTSServerSettings.natsPort}",
        topic = TTSServerSettings.topic,
        group = TTSServerSettings.group,
        parallelism = TTSServerSettings.parallelism,
        messageHandler = TTSMessageHandler(
            repository = DirectTTSResourceRepository(
                service = createTTSService(
                    cache = RedisTTSResourceCache(redis.stringToByteArrayCommands),
                    onGetResource = { onGetResource(redis.stringToStringCommands) },
                )
            )
        ),
        onShutdown = {
            redis.close()
        }
    )
}