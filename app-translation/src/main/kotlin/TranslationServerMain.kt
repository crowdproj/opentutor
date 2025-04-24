package com.gitlab.sszuev.flashcards.translation

import com.gitlab.sszuev.flashcards.translation.impl.createTranslationRepository
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.translation.TranslationServerMain")

fun main() {
    val connectionUrl = "nats://${TranslationServerSettings.host}:${TranslationServerSettings.port}"
    val processor = TranslationServerProcessor(
        repository = createTranslationRepository(),
        topic = TranslationServerSettings.topic,
        group = TranslationServerSettings.group,
        connectionFactory = {
            val options = Options.Builder()
                .server(connectionUrl)
                .maxReconnects(-1)
                .reconnectWait(Duration.ofSeconds(2))
                .pingInterval(Duration.ofSeconds(10))
                .connectionListener { conn, type -> logger.warn("NATS event: $type | Status: ${conn.status}") }
                .build()
            Nats.connect(options).also {
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