package com.gitlab.sszuev.flashcards.config

import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbUserRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbUserRepository
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.createLocalTTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.rabbitmq.RMQTTSResourceRepository

data class RepositoriesConfig(
    val prodTTSClientRepository: TTSResourceRepository = RMQTTSResourceRepository(),
    val testTTSClientRepository: TTSResourceRepository = createLocalTTSResourceRepository(),
    val prodCardRepository: DbCardRepository = PgDbCardRepository(),
    val testCardRepository: DbCardRepository = MemDbCardRepository(),
    val prodDictionaryRepository: DbDictionaryRepository = PgDbDictionaryRepository(),
    val testDictionaryRepository: DbDictionaryRepository = MemDbDictionaryRepository(),
    val prodUserRepository: DbUserRepository = PgDbUserRepository(),
    val testUserRepository: DbUserRepository = MemDbUserRepository(),
) {

    val cardRepositories by lazy {
        CardRepositories(
            prodTTSClientRepository = this.prodTTSClientRepository,
            testTTSClientRepository = this.testTTSClientRepository,
            prodCardRepository = this.prodCardRepository,
            testCardRepository = this.testCardRepository,
            prodUserRepository = this.prodUserRepository,
            testUserRepository = this.testUserRepository,
        )
    }

    val dictionaryRepositories by lazy {
        DictionaryRepositories(
            prodDictionaryRepository = this.prodDictionaryRepository,
            testDictionaryRepository = this.testDictionaryRepository,
            prodUserRepository = this.prodUserRepository,
            testUserRepository = this.testUserRepository,
        )
    }
}