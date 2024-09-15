package com.gitlab.sszuev.flashcards.settings

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbUserRepository
import io.nats.client.Nats
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger(SettingsServerSettings::class.java)

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
    SettingsServerController(processor).start()
}