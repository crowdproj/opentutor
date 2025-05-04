package com.gitlab.sszuev.flashcards.speaker

import io.lettuce.core.api.sync.RedisCommands
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RedisTTSResourceCache::class.java)

class RedisTTSResourceCache(
    private val commands: RedisCommands<String, ByteArray>
) : TTSResourceCache {

    override fun get(id: String): ByteArray? = try {
        commands.get(id)
    } catch (ex: Exception) {
        logger.error("unexpected error while redis#get", ex)
        null
    }

    override fun put(id: String, data: ByteArray) {
        try {
            if (commands.set(id, data) != "OK") {
                logger.error("Can't redis#set")
            }
        } catch (ex: Exception) {
            logger.error("unexpected error while redis#put", ex)
        }
    }
}