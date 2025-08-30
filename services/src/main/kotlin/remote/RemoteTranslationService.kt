package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.TranslationContext
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
    private val connection: Connection,
) : TranslationService {
    constructor() : this(
        topic = ServicesConfig.translationNatsTopic,
        requestTimeoutInMillis = ServicesConfig.fastRequestTimeoutInMilliseconds,
        connection = NatsConnector.connection,
    )

    override suspend fun fetchTranslation(context: TranslationContext): TranslationContext = context.exec()

    private suspend fun TranslationContext.exec(): TranslationContext {
        val answer = withContext(Dispatchers.IO) {
            connection.request(
                /* subject = */ topic,
                /* body = */ this@exec.toByteArray(),
                /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
            )
        }
        val res = translationContextFromByteArray(answer!!.data)
        res.copyTo(this)
        return this
    }

    private fun TranslationContext.copyTo(target: TranslationContext) {
        target.responseCardEntity = this.responseCardEntity
        target.errors.addAll(this.errors)
        target.status = this.status
    }
}