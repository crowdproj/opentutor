package com.gitlab.sszuev.flashcards.dictionaries

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.core.DictionaryCorProcessor
import com.gitlab.sszuev.flashcards.utils.dictionaryContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
import io.nats.client.Connection
import io.nats.client.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

private val logger = LoggerFactory.getLogger(DictionariesServerSettings::class.java)

class DictionariesServerProcessor(
    private val topic: String,
    private val group: String,
    private val repositories: DbRepositories,
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
    private val corProcessor = DictionaryCorProcessor()

    fun ready() = run.get()

    suspend fun process(coroutineContext: CoroutineContext) {
        val dispatcher = connection.createDispatcher { msg: Message ->
            CoroutineScope(coroutineContext).launch {
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