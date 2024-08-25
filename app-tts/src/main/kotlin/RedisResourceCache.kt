package com.gitlab.sszuev.flashcards.speaker

import io.lettuce.core.api.sync.RedisCommands
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(RedisResourceCache::class.java)

class RedisResourceCache(
    private val commands: RedisCommands<String, ByteArray>
) : ResourceCache {

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