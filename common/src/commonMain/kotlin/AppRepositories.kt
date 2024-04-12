package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpTTSResourceRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

data class AppRepositories(
    val cardRepository: DbCardRepository = NoOpDbCardRepository,
    val dictionaryRepository: DbDictionaryRepository = NoOpDbDictionaryRepository,
    val ttsClientRepository: TTSResourceRepository = NoOpTTSResourceRepository,
) {

    companion object {
        val NO_OP_REPOSITORIES = AppRepositories()
    }
}