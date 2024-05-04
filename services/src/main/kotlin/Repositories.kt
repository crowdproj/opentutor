package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.AppRepositories
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbCardRepository
import com.gitlab.sszuev.flashcards.dbpg.PgDbDictionaryRepository
import com.gitlab.sszuev.flashcards.speaker.NatsTTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.createDirectTTSResourceRepository

val localAppRepositories: AppRepositories by lazy {
    AppRepositories(
        cardRepository = MemDbCardRepository(),
        dictionaryRepository = MemDbDictionaryRepository(),
        ttsClientRepository = createDirectTTSResourceRepository(),
    )
}

val remoteAppRepositories: AppRepositories by lazy {
    AppRepositories(
        cardRepository = PgDbCardRepository(),
        dictionaryRepository = PgDbDictionaryRepository(),
        ttsClientRepository = NatsTTSResourceRepository(),
    )
}