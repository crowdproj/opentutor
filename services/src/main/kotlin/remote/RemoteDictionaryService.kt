package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.NatsConnectionFactory
import com.gitlab.sszuev.flashcards.services.ServiceSettings
import com.gitlab.sszuev.flashcards.utils.dictionaryContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.temporal.ChronoUnit

class RemoteDictionaryService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    connectionFactory: () -> Connection,
) : DictionaryService {
    constructor() : this(
        topic = ServiceSettings.dictionariesNatsTopic,
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

    override suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun createDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun updateDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun uploadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        val answer = withContext(Dispatchers.IO) {
            connection.request(
                /* subject = */ topic,
                /* body = */ this@exec.toByteArray(),
                /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
            )
        }
        val res = dictionaryContextFromByteArray(answer.data)
        res.copyTo(this)
        return this
    }

    private fun DictionaryContext.copyTo(target: DictionaryContext) {
        target.responseDictionaryEntity = this.responseDictionaryEntity
        target.responseDictionaryEntityList = this.responseDictionaryEntityList
        target.responseDictionaryResourceEntity = this.responseDictionaryResourceEntity
        target.errors.addAll(this.errors)
        target.status = this.status
    }
}