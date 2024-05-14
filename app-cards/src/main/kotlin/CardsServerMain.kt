package com.gitlab.sszuev.flashcards.cards

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import io.nats.client.Nats
import org.slf4j.LoggerFactory
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
    CardsServerController(processor).start()
}