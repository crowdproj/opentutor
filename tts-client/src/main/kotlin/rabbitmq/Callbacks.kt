package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private var logger = LoggerFactory.getLogger("RabbitmqCallbacks")

internal fun Channel.deliverCallback(
    publishMessage: suspend Channel.(String, Delivery) -> Unit,
    onError: (String, Delivery, Throwable) -> Unit = { tag, _, ex ->
        logger.error("Error while processing [$tag]", ex)
    },
): DeliverCallback = DeliverCallback { tag, message ->
    runBlocking {
        runCatching {
            publishMessage(tag, message)
        }.onFailure {
            onError(tag, message, it)
        }
    }
}

internal fun cancelCallback() = CancelCallback {
    logger.info("[$it] was cancelled.")
}