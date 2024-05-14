package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.createDirectTTSResourceRepository

val localDbRepositories: DbRepositories by lazy {
    DbRepositories(
        cardRepository = MemDbCardRepository(),
        dictionaryRepository = MemDbDictionaryRepository(),
    )
}

val localTTSRepository: TTSResourceRepository by lazy {
    createDirectTTSResourceRepository()
}