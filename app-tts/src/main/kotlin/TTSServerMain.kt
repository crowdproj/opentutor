package com.gitlab.sszuev.flashcards.speaker

import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration
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
        connectionFactory = {
            val options = Options.Builder()
                .server(natsConfig.url)
                .maxReconnects(-1)
                .reconnectWait(Duration.ofSeconds(2))
                .pingInterval(Duration.ofSeconds(10))
                .connectionListener { conn, type -> logger.warn("NATS event: $type | Status: ${conn.status}") }
                .build()
            Nats.connect(options).also {
                logger.info("Nats connection established: ${natsConfig.url}")
            }
        }
    )
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Close connections on shutdown.")
        processor.close()
        redis.close()
    })
    logger.info("Start processing.")
    TTSServerController(processor).start()
}