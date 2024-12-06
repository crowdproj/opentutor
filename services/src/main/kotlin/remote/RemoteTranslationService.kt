package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.services.NatsConnectionFactory
import com.gitlab.sszuev.flashcards.services.ServicesConfig
import com.gitlab.sszuev.flashcards.services.TranslationService
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.translationContextFromByteArray
import io.nats.client.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.temporal.ChronoUnit

class RemoteTranslationService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    connectionFactory: () -> Connection,
) : TranslationService {
    constructor() : this(
        topic = ServicesConfig.translationNatsTopic,
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

    override suspend fun fetchTranslation(context: TranslationContext): TranslationContext = context.exec()

    private suspend fun TranslationContext.exec(): TranslationContext {
        val answer = withContext(Dispatchers.IO) {
            connection.request(
                /* subject = */ topic,
                /* body = */ this@exec.toByteArray(),
                /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
            )
        }
        val res = translationContextFromByteArray(answer.data)
        res.copyTo(this)
        return this
    }

    private fun TranslationContext.copyTo(target: TranslationContext) {
        target.responseCardEntity = this.responseCardEntity
        target.errors.addAll(this.errors)
        target.status = this.status
    }
}