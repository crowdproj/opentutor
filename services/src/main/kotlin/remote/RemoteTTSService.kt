package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.services.NatsConnectionFactory
import com.gitlab.sszuev.flashcards.services.ServicesConfig
import com.gitlab.sszuev.flashcards.services.TTSService
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.ttsContextFromByteArray
import io.nats.client.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.temporal.ChronoUnit

class RemoteTTSService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    connectionFactory: () -> Connection,
) : TTSService {
    constructor() : this(
        topic = ServicesConfig.ttsNatsTopic,
        requestTimeoutInMillis = ServicesConfig.requestTimeoutInMilliseconds,
        connectionFactory = { NatsConnectionFactory.connection }
    )

    private val connection: Connection by lazy {
        connectionFactory().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
        }
    }

    override suspend fun getResource(context: TTSContext) = context.exec()

    private suspend fun TTSContext.exec(): TTSContext {
        val answer = withContext(Dispatchers.IO) {
            connection.request(
                /* subject = */ topic,
                /* body = */ this@exec.toByteArray(),
                /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
            )
        }
        val res = ttsContextFromByteArray(answer.data)
        res.copyTo(this)
        return this
    }

    private fun TTSContext.copyTo(target: TTSContext) {
        target.responseTTSResourceEntity = this.responseTTSResourceEntity
        target.normalizedRequestTTSResourceGet = this.normalizedRequestTTSResourceGet
        target.errors.addAll(this.errors)
        target.status = this.status
    }
}