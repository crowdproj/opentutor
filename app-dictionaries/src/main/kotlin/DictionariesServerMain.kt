package com.gitlab.sszuev.flashcards.dictionaries

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDocumentRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbUserRepository
import com.gitlab.sszuev.flashcards.nats.runApp

suspend fun main() = runApp(
    connectionUrl = "nats://${DictionariesServerSettings.host}:${DictionariesServerSettings.port}",
    topic = DictionariesServerSettings.topic,
    group = DictionariesServerSettings.group,
    parallelism = DictionariesServerSettings.parallelism,
    withDbHealthCheck = true,
    messageHandler = DictionariesMessageHandler(
        repositories = DbRepositories(
            cardRepository = PgDbCardRepository().also { it.connect() },
            dictionaryRepository = PgDbDictionaryRepository().also { it.connect() },
            userRepository = PgDbUserRepository().also { it.connect() },
            documentRepository = PgDbDocumentRepository().also { it.connect() },
        )
    )
)