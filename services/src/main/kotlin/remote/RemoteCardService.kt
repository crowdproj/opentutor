package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.ServicesConfig
import com.gitlab.sszuev.flashcards.utils.cardContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection

class RemoteCardService(
    private val topic: String,
    private val requestTimeoutInMillis: Long,
    private val connection: Connection,
) : CardService {
    constructor() : this(
        topic = ServicesConfig.cardsNatsTopic,
        requestTimeoutInMillis = ServicesConfig.requestTimeoutInMilliseconds,
        connection = NatsConnectionFactory.connection,
    )

    override suspend fun createCard(context: CardContext): CardContext = context.exec()
    override suspend fun updateCard(context: CardContext): CardContext = context.exec()
    override suspend fun searchCards(context: CardContext): CardContext = context.exec()
    override suspend fun getAllCards(context: CardContext): CardContext = context.exec()
    override suspend fun getCard(context: CardContext): CardContext = context.exec()
    override suspend fun learnCard(context: CardContext): CardContext = context.exec()
    override suspend fun resetCard(context: CardContext): CardContext = context.exec()
    override suspend fun deleteCard(context: CardContext): CardContext = context.exec()
    override suspend fun resetAllCards(context: CardContext): CardContext = context.exec()

    private suspend fun CardContext.exec(): CardContext {
        val answer = connection.requestWithRetry(
            topic = topic,
            data = this@exec.toByteArray(),
            requestTimeoutInMillis = requestTimeoutInMillis,
        )
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