package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.core.TTSCorProcessor
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.nats.MessageHandler
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.ttsContextFromByteArray
import io.nats.client.Connection
import io.nats.client.Message
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(TTSMessageHandler::class.java)

class TTSMessageHandler(
    private val repository: TTSResourceRepository
) : MessageHandler {
    private val processor = TTSCorProcessor()

    override suspend fun handleMessage(connection: Connection, msg: Message) {
        try {
            val context = ttsContextFromByteArray(msg.data)
            context.repository = repository
            processor.execute(context)
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
                TTSContext().apply { errors += AppError(message = "Unexpected error", exception = ex) }
            connection.publish(msg.replyTo, context.toByteArray())
        }
    }
}