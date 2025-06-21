package com.gitlab.sszuev.flashcards.services.remote

import io.nats.client.Connection
import io.nats.client.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.time.Duration

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.services.remote.NatsSupportKt")

suspend fun Connection.requestWithRetry(
    topic: String,
    data: ByteArray,
    requestTimeoutInMillis: Long,
    maxAttempts: Int = 4,
    baseDelayMillis: Long = 200L,
): Message {

    var lastError: Throwable? = null

    repeat(maxAttempts) { attempt ->
        try {
            val answer = withContext(Dispatchers.IO) {
                request(
                    /* subject = */ topic,
                    /* body = */ data,
                    /* timeout = */ Duration.ofMillis(requestTimeoutInMillis),
                )
            }
            if (answer != null) {
                return answer
            } else {
                logger.warn("Attempt ${attempt + 1}/$maxAttempts: Received null response from NATS.")
            }
        } catch (ex: Exception) {
            lastError = ex
            logger.warn("Attempt ${attempt + 1}/$maxAttempts failed: ${ex::class.simpleName} -- ${ex.message}")
        }

        delay(baseDelayMillis * (1 shl attempt)) // backoff: 200, 400, 800, ...
    }

    throw IllegalStateException("NATS request failed after $maxAttempts attempts", lastError)
}