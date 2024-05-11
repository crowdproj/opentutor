package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbDictionaryRepository

data class DbRepositories(
    val cardRepository: DbCardRepository = NoOpDbCardRepository,
    val dictionaryRepository: DbDictionaryRepository = NoOpDbDictionaryRepository,
) {

    companion object {
        val NO_OP_REPOSITORIES = DbRepositories()
    }
}