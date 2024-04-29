package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import io.nats.client.Connection
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit

private val logger = LoggerFactory.getLogger(NatsTTSResourceRepository::class.java)

class NatsTTSResourceRepository(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    connectionFactory: () -> Connection,
) : TTSResourceRepository {

    constructor() : this(
        topic = TTSClientSettings.topic,
        requestTimeoutInMillis = TTSClientSettings.requestTimeoutInMilliseconds,
        connectionFactory = { NatsConnectionFactory.connection }
    )

    private val connection: Connection by lazy {
        connectionFactory().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
        }
    }

    override suspend fun findResource(lang: String, word: String): ByteArray = getResource(id(lang, word))

    fun getResource(id: String): ByteArray {
        if (logger.isDebugEnabled) {
            logger.debug("Request: '{}'", id)
        }
        val answer = connection.request(
            /* subject = */ topic,
            /* body = */ id.toByteArray(Charsets.UTF_8),
            /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
        )
        val data = answer.data ?: throw throw NotFoundResourceException(id, "empty result for request.")
        if (data.startsWith(EXCEPTION_PREFIX)) {
            throw ServerResourceException(id, data.toString(Charsets.UTF_8))
        }
        return data
    }

    companion object {
        private val EXCEPTION_PREFIX = "e:".toByteArray(Charsets.UTF_8)

        private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
            if (this.size < prefix.size) {
                return false
            }
            for (i in prefix.indices) {
                if (this[i] != prefix[i]) {
                    return false
                }
            }
            return true
        }

        private fun id(lang: String, word: String): String {
            return "${lang}:${word}"
        }
    }
}