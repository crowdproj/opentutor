package com.gitlab.sszuev.flashcards.translation

import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands

class TranslationRedisConnectionFactory(
    connectionUrl: String = "redis://localhost:6379/1",
) : AutoCloseable {

    private val client by lazy {
        RedisClient.create(connectionUrl)
    }
    private val stringToStringConnection by lazy {
        client.connect()
    }

    val stringToStringCommands: RedisCommands<String, String> by lazy {
        stringToStringConnection.sync()
    }

    override fun close() {
        stringToStringConnection.close()
        client.shutdown()
    }
}