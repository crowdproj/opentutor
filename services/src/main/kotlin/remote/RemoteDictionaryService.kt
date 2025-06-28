package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.services.DictionaryService
import com.gitlab.sszuev.flashcards.services.ServicesConfig
import com.gitlab.sszuev.flashcards.utils.dictionaryContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection

class RemoteDictionaryService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    private val connection: Connection,
) : DictionaryService {
    constructor() : this(
        topic = ServicesConfig.dictionariesNatsTopic,
        requestTimeoutInMillis = ServicesConfig.mainRequestTimeoutInMilliseconds,
        connection = NatsConnector.connection
    )

    override suspend fun getAllDictionaries(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun createDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun updateDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun deleteDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun downloadDictionary(context: DictionaryContext): DictionaryContext = context.exec()
    override suspend fun uploadDictionary(context: DictionaryContext): DictionaryContext = context.exec()

    private suspend fun DictionaryContext.exec(): DictionaryContext {
        val answer = connection.requestWithRetry(
            topic = topic,
            data = this@exec.toByteArray(),
            requestTimeoutInMillis = requestTimeoutInMillis,
        )
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