package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.translation.impl.LingueeTranslationRepository
import io.nats.client.Nats
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger(TranslationServerSettings::class.java)

fun main() {
    val connectionUrl = "nats://${TranslationServerSettings.host}:${TranslationServerSettings.port}"
    val processor = TranslationServerProcessor(
        repository = LingueeTranslationRepository(),
        topic = TranslationServerSettings.topic,
        group = TranslationServerSettings.group,
        connectionFactory = {
            Nats.connectReconnectOnConnect(connectionUrl).also {
                logger.info("Nats connection established: $connectionUrl")
            }
        }
    )
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Close connection on shutdown.")
        processor.close()
    })
    logger.info("Start processing.")
    TranslationServerController(processor).start()
}