package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpTTSResourceRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

data class AppRepositories(
    private val prodTTSClientRepository: TTSResourceRepository = NoOpTTSResourceRepository,
    private val testTTSClientRepository: TTSResourceRepository = NoOpTTSResourceRepository,
    private val prodDictionaryRepository: DbDictionaryRepository = NoOpDbDictionaryRepository,
    private val testDictionaryRepository: DbDictionaryRepository = NoOpDbDictionaryRepository,
    private val prodCardRepository: DbCardRepository = NoOpDbCardRepository,
    private val testCardRepository: DbCardRepository = NoOpDbCardRepository,
) {

    companion object {
        val NO_OP_REPOSITORIES = AppRepositories()
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

    fun ttsClientRepository(mode: AppMode): TTSResourceRepository {
        return when (mode) {
            AppMode.PROD -> prodTTSClientRepository
            AppMode.TEST -> testTTSClientRepository
            AppMode.STUB -> NoOpTTSResourceRepository
        }
    }
}