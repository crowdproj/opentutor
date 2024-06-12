package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbUserRepository

data class DbRepositories(
    val cardRepository: DbCardRepository = NoOpDbCardRepository,
    val dictionaryRepository: DbDictionaryRepository = NoOpDbDictionaryRepository,
    val userRepository: DbUserRepository = NoOpDbUserRepository,
) {

    companion object {
        val NO_OP_REPOSITORIES = DbRepositories()
    }
}