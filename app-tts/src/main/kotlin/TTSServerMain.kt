package com.gitlab.sszuev.flashcards.speaker

import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.speaker.TTSServerMain")

fun main() {
    val natsConfig = NatsConfig()
    val redisConfig = RedisConfig()
    val redis = RedisConnectionFactory(
        connectionUrl = redisConfig.url,
    )
    val processor = NatsTTSServerProcessorImpl(
        service = createTTSService(
            cache = RedisResourceCache(redis.stringToByteArrayCommands),
            onGetResource = { onGetResource(redis.stringToStringCommands) },
        ),
        topic = natsConfig.topic,
        group = natsConfig.group,
        connectionUrl = natsConfig.url,
    )
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Close connections on shutdown.")
        processor.close()
        redis.close()
    })
    logger.info("Start processing.")
    TTSServerController(processor).start()
}