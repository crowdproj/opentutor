package com.gitlab.sszuev.flashcards.cards

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import io.nats.client.Nats
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.cards.CardsServerMain")

fun main() {
    val natsConnectionUrl = "nats://${ServerSettings.host}:${ServerSettings.port}"
    val processor = CardsServerProcessor(
        repositories = DbRepositories(
            cardRepository = PgDbCardRepository(),
            dictionaryRepository = PgDbDictionaryRepository()
        ),
        topic = ServerSettings.topic,
        group = ServerSettings.group,
        connectionFactory = { Nats.connectReconnectOnConnect(natsConnectionUrl) }
    )
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Close connection on shutdown.")
        processor.close()
    })
    logger.info("Start processing.")
    CardsServerController(processor).start()
}