package com.gitlab.sszuev.flashcards.settings

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbUserRepository
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.settings.SettingsServerMain")

fun main() {
    val connectionUrl = "nats://${SettingsServerSettings.host}:${SettingsServerSettings.port}"
    val processor = SettingsServerProcessor(
        repositories = DbRepositories(
            cardRepository = PgDbCardRepository().also { it.connect() },
            dictionaryRepository = PgDbDictionaryRepository().also { it.connect() },
            userRepository = PgDbUserRepository().also { it.connect() },
        ),
        topic = SettingsServerSettings.topic,
        group = SettingsServerSettings.group,
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
    SettingsServerController(processor).start()
}