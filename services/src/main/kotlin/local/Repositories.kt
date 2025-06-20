package com.gitlab.sszuev.flashcards.services.local

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.dbmem.MemDbCardRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbDocumentRepository
import com.gitlab.sszuev.flashcards.dbmem.MemDbUserRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.createDirectTTSResourceRepository
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import com.gitlab.sszuev.flashcards.translation.impl.createTranslationRepository

val localDbRepositories: DbRepositories by lazy {
    DbRepositories(
        cardRepository = MemDbCardRepository(),
        dictionaryRepository = MemDbDictionaryRepository(),
        userRepository = MemDbUserRepository(),
        documentRepository = MemDbDocumentRepository(),
    )
}

val localTTSRepository: TTSResourceRepository by lazy {
    createDirectTTSResourceRepository()
}

val localTranslationRepository: TranslationRepository by lazy {
    createTranslationRepository()
}