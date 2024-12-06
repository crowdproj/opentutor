package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbUserRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.createDirectTTSResourceRepository
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import com.gitlab.sszuev.flashcards.translation.impl.LingueeTranslationRepository

val localDbRepositories: DbRepositories by lazy {
    DbRepositories(
        cardRepository = MemDbCardRepository(),
        dictionaryRepository = MemDbDictionaryRepository(),
        userRepository = MemDbUserRepository(),
    )
}

val localTTSRepository: TTSResourceRepository by lazy {
    createDirectTTSResourceRepository()
}

val localTranslationRepository: TranslationRepository by lazy {
    LingueeTranslationRepository()
}