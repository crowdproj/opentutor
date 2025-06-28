package com.gitlab.sszuev.flashcards.cards

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.nats.runApp

suspend fun main() = runApp(
    connectionUrl = "nats://${CardsServerSettings.host}:${CardsServerSettings.port}",
    topic = CardsServerSettings.topic,
    group = CardsServerSettings.group,
    parallelism = CardsServerSettings.parallelism,
    withDbHealthCheck = true,
    messageHandler = CardsMessageHandler(
        repositories = DbRepositories(
            cardRepository = PgDbCardRepository().apply { connect() },
            dictionaryRepository = PgDbDictionaryRepository().apply { connect() }
        )
    )
)