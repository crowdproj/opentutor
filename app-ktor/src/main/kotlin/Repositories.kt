package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.speaker.createDirectTTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.rabbitmq.RMQTTSResourceRepository

fun appRepositories() = AppRepositories(
    prodTTSClientRepository = RMQTTSResourceRepository(),
    testTTSClientRepository = createDirectTTSResourceRepository(),
    prodCardRepository = PgDbCardRepository(),
    testCardRepository = MemDbCardRepository(),
    prodDictionaryRepository = PgDbDictionaryRepository(),
    testDictionaryRepository = MemDbDictionaryRepository(),
)