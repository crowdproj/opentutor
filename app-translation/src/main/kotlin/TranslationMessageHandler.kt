package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.core.TranslationCorProcessor
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.nats.MessageHandler
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.translationContextFromByteArray
import io.nats.client.Connection
import io.nats.client.Message
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(TranslationMessageHandler::class.java)

class TranslationMessageHandler(private val repository: TranslationRepository) : MessageHandler {
    private val corProcessor = TranslationCorProcessor()

    override suspend fun handleMessage(connection: Connection, msg: Message) {
        try {
            val context = translationContextFromByteArray(msg.data)
            if (logger.isDebugEnabled) {
                logger.debug("Processing ${context.requestId}")
            }
            context.repository = repository
            corProcessor.execute(context)
            context.errors.forEach {
                logger.error("$it")
                it.exception?.let { ex ->
                    logger.error("Processing exception ::: ${ex.message}", ex)
                }
            }
            connection.publish(msg.replyTo, context.toByteArray())
        } catch (ex: Exception) {
            logger.error("Unexpected error", ex)
            val context =
                TranslationContext().apply { errors += AppError(message = "Unexpected error", exception = ex) }
            connection.publish(msg.replyTo, context.toByteArray())
        }
        if (logger.isDebugEnabled) {
            logger.debug("Message sent (subject = ${msg.replyTo})")
        }
    }
}