package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.services.NatsConnectionFactory
import com.gitlab.sszuev.flashcards.services.ServiceSettings
import com.gitlab.sszuev.flashcards.services.TTSService
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.ttsContextFromByteArray
import io.nats.client.Connection
import java.time.Duration
import java.time.temporal.ChronoUnit

class RemoteTTSService(
    private val ttsTopic: String,
    private val requestTimeoutInMillis: Long,
    connectionFactory: () -> Connection,
) : TTSService {
    constructor() : this(
        ttsTopic = ServiceSettings.ttsTopic,
        requestTimeoutInMillis = ServiceSettings.requestTimeoutInMilliseconds,
        connectionFactory = { NatsConnectionFactory.connection }
    )

    private val connection: Connection by lazy {
        connectionFactory().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
        }
    }

    override suspend fun getResource(context: TTSContext): TTSContext {
        val answer = connection.request(
            /* subject = */ ttsTopic,
            /* body = */ context.toByteArray(),
            /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
        )
        val res = ttsContextFromByteArray(answer.data)
        context.responseTTSResourceEntity = res.responseTTSResourceEntity
        context.normalizedRequestTTSResourceGet = res.normalizedRequestTTSResourceGet
        context.errors.addAll(res.errors)
        return context
    }

}