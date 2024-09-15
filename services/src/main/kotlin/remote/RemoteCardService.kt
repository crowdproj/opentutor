package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.NatsConnectionFactory
import com.gitlab.sszuev.flashcards.services.ServicesConfig
import com.gitlab.sszuev.flashcards.utils.cardContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.temporal.ChronoUnit

class RemoteCardService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    connectionFactory: () -> Connection,
) : CardService {

    constructor() : this(
        topic = ServicesConfig.cardsNatsTopic,
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

    override suspend fun createCard(context: CardContext): CardContext = context.exec()
    override suspend fun updateCard(context: CardContext): CardContext = context.exec()
    override suspend fun searchCards(context: CardContext): CardContext = context.exec()
    override suspend fun getAllCards(context: CardContext): CardContext = context.exec()
    override suspend fun getCard(context: CardContext): CardContext = context.exec()
    override suspend fun learnCard(context: CardContext): CardContext = context.exec()
    override suspend fun resetCard(context: CardContext): CardContext = context.exec()
    override suspend fun deleteCard(context: CardContext): CardContext = context.exec()

    private suspend fun CardContext.exec(): CardContext {
        val answer = withContext(Dispatchers.IO) {
            connection.request(
                /* subject = */ topic,
                /* body = */ this@exec.toByteArray(),
                /* timeout = */ Duration.of(requestTimeoutInMillis, ChronoUnit.MILLIS),
            )
        }
        val res = cardContextFromByteArray(answer.data)
        res.copyTo(this)
        return this
    }

    private fun CardContext.copyTo(target: CardContext) {
        target.responseCardEntity = this.responseCardEntity
        target.responseCardEntityList = this.responseCardEntityList
        target.errors.addAll(this.errors)
        target.status = this.status
    }
}