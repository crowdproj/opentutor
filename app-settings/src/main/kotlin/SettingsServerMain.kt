package com.gitlab.sszuev.flashcards.settings

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbUserRepository
import com.gitlab.sszuev.flashcards.nats.runProcessing

suspend fun main() = runProcessing(
    connectionUrl = "nats://${SettingsServerSettings.host}:${SettingsServerSettings.port}",
    topic = SettingsServerSettings.topic,
    group = SettingsServerSettings.group,
    parallelism = SettingsServerSettings.parallelism,
    messageHandler = SettingsMessageHandler(
        DbRepositories(
            cardRepository = PgDbCardRepository().also { it.connect() },
            dictionaryRepository = PgDbDictionaryRepository().also { it.connect() },
            userRepository = PgDbUserRepository().also { it.connect() },
        )
    )
)