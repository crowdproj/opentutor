package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRepositories
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbUserRepository

data class DictionaryRepositories(
    private val prodDictionaryRepository: DbDictionaryRepository = NoOpDbDictionaryRepository,
    private val testDictionaryRepository: DbDictionaryRepository = NoOpDbDictionaryRepository,
    private val prodUserRepository: DbUserRepository = NoOpDbUserRepository,
    private val testUserRepository: DbUserRepository = NoOpDbUserRepository,
    private val prodCardRepository: DbCardRepository = NoOpDbCardRepository,
    private val testCardRepository: DbCardRepository = NoOpDbCardRepository,
): AppRepositories {
    companion object {
        val NO_OP_REPOSITORIES = DictionaryRepositories()
    }

    override fun userRepository(mode: AppMode): DbUserRepository {
        return when(mode) {
            AppMode.PROD -> prodUserRepository
            AppMode.TEST -> testUserRepository
            AppMode.STUB -> NoOpDbUserRepository
        }
    }

    fun dictionaryRepository(mode: AppMode): DbDictionaryRepository {
        return when (mode) {
            AppMode.PROD -> prodDictionaryRepository
            AppMode.TEST -> testDictionaryRepository
            AppMode.STUB -> NoOpDbDictionaryRepository
        }
    }

    fun cardRepository(mode: AppMode): DbCardRepository {
        return when (mode) {
            AppMode.PROD -> prodCardRepository
            AppMode.TEST -> testCardRepository
            AppMode.STUB -> NoOpDbCardRepository
        }
    }
}