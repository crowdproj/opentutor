package com.gitlab.sszuev.flashcards.dictionaries

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.nats.MessageHandler
import com.gitlab.sszuev.flashcards.utils.dictionaryContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection
import io.nats.client.Message
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DictionariesMessageHandler::class.java)

class DictionariesMessageHandler(
    private val repositories: DbRepositories,
) : MessageHandler {
    private val corProcessor = DictionaryCorProcessor()

    override suspend fun handleMessage(connection: Connection, msg: Message) {
        try {
            val context = dictionaryContextFromByteArray(msg.data)
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
                DictionaryContext().apply { errors += AppError(message = "Unexpected error", exception = ex) }
            connection.publish(msg.replyTo, context.toByteArray())
        }
    }
}