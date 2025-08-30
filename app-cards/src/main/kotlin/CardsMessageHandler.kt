package com.gitlab.sszuev.flashcards.cards

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.core.CardCorProcessor
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.nats.MessageHandler
import com.gitlab.sszuev.flashcards.utils.cardContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection
import io.nats.client.Message
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

private val logger = LoggerFactory.getLogger(CardsMessageHandler::class.java)

@OptIn(ExperimentalTime::class)
class CardsMessageHandler(
    private val repositories: DbRepositories,
) : MessageHandler {
    private val corProcessor = CardCorProcessor()

    override suspend fun handleMessage(connection: Connection, msg: Message) {
        try {
            val context = cardContextFromByteArray(msg.data)
            if (logger.isDebugEnabled) {
                logger.debug("Processing ${context.requestId}")
            }
            context.repositories = repositories
            corProcessor.execute(context)
            context.errors.forEach {
                logger.error("$it")
                it.exception?.let { ex ->
                    logger.error("Exception: ${ex.message}", ex)
                }
            }
            connection.publish(msg.replyTo, context.toByteArray())
        } catch (ex: Exception) {
            logger.error("Unexpected error", ex)
            val context =
                CardContext().apply { errors += AppError(message = "Unexpected error", exception = ex) }
            connection.publish(msg.replyTo, context.toByteArray())
        }
    }
}