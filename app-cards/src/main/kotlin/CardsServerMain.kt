package com.gitlab.sszuev.flashcards.cards

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.cards.CardsServerMain")

fun main() {
    val connectionUrl = "nats://${CardsServerSettings.host}:${CardsServerSettings.port}"
    val processor = CardsServerProcessor(
        repositories = DbRepositories(
            cardRepository = PgDbCardRepository().also { it.connect() },
            dictionaryRepository = PgDbDictionaryRepository().also { it.connect() }
        ),
        topic = CardsServerSettings.topic,
        group = CardsServerSettings.group,
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
    CardsServerController(processor).start()
}