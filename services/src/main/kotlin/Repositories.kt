package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.NatsTTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.createDirectTTSResourceRepository

val localDbRepositories: DbRepositories by lazy {
    DbRepositories(
        cardRepository = MemDbCardRepository(),
        dictionaryRepository = MemDbDictionaryRepository(),
    )
}

val remoteDbRepositories: DbRepositories by lazy {
    DbRepositories(
        cardRepository = PgDbCardRepository(),
        dictionaryRepository = PgDbDictionaryRepository()
    )
}

val localTTSRepository: TTSResourceRepository by lazy {
    createDirectTTSResourceRepository()
}

val remoteTTSRepository: TTSResourceRepository by lazy {
    NatsTTSResourceRepository()
}