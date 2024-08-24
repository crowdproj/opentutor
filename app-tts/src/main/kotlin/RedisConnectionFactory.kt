package com.gitlab.sszuev.flashcards.speaker

import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec

class RedisConnectionFactory(
    connectionUrl: String = "redis://localhost:6379",
) : AutoCloseable {

    private val client by lazy {
        RedisClient.create(connectionUrl)
    }
    private val stringToStringConnection by lazy {
        client.connect()
    }
    private val stringToByteArrayConnection by lazy {
        client.connect(RedisCodec.of(StringCodec(), ByteArrayCodec()))
    }

    val stringToByteArrayCommands: RedisCommands<String, ByteArray> by lazy {
        stringToByteArrayConnection.sync()
    }

    val stringToStringCommands: RedisCommands<String, String> by lazy {
        stringToStringConnection.sync()
    }

    override fun close() {
        stringToByteArrayConnection.close()
        stringToStringConnection.close()
        client.shutdown()
    }
}