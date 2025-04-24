package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.core.TranslationCorProcessor
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.translationContextFromByteArray
import io.nats.client.Connection
import io.nats.client.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

private val logger = LoggerFactory.getLogger(TranslationCorProcessor::class.java)

class TranslationServerProcessor(
    private val topic: String,
    private val group: String,
    private val repository: TranslationRepository,
    connectionFactory: () -> Connection,
) : AutoCloseable {
    private val run = AtomicBoolean(false)
    private val connection by lazy {
        connectionFactory().also {
            check(it.status == Connection.Status.CONNECTED) {
                "connection status: ${it.status}"
            }
        }
    }
    private val corProcessor = TranslationCorProcessor()

    fun ready() = run.get()

    suspend fun process(coroutineContext: CoroutineContext) {
        val dispatcher = connection.createDispatcher { msg: Message ->
            CoroutineScope(coroutineContext).launch {
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
                            logger.error("Exception: ${ex.message}", ex)
                        }
                    }
                    connection.publish(msg.replyTo, context.toByteArray())
                } catch (ex: Exception) {
                    logger.error("Unexpected error", ex)
                    val context =
                        TranslationContext().apply { errors += AppError(message = "Unexpected error", exception = ex) }
                    connection.publish(msg.replyTo, context.toByteArray())
                }
            }
        }
        dispatcher.subscribe(topic, group)
        run.set(true)
        while (run.get()) {
            runCatching {
                delay(timeMillis = 100)
            }.onFailure { ex ->
                if (ex is CancellationException) {
                    logger.info("The lifecycle job is canceled.")
                } else {
                    logger.error("Unexpected error", ex)
                }
            }
        }
    }

    override fun close() {
        logger.info("Close.")
        run.set(false)
        connection.close()
    }
}