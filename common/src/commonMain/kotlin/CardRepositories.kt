package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRepositories
import com.gitlab.sszuev.flashcards.repositories.*

data class CardRepositories(
    private val prodTTSClientRepository: TTSResourceRepository = NoOpTTSResourceRepository,
    private val testTTSClientRepository: TTSResourceRepository = NoOpTTSResourceRepository,
    private val prodCardRepository: DbCardRepository = NoOpDbCardRepository,
    private val testCardRepository: DbCardRepository = NoOpDbCardRepository,
    private val prodUserRepository: DbUserRepository = NoOpDbUserRepository,
    private val testUserRepository: DbUserRepository = NoOpDbUserRepository,
): AppRepositories {
    companion object {
        val NO_OP_REPOSITORIES = CardRepositories()
    }

    override fun userRepository(mode: AppMode): DbUserRepository {
        return when(mode) {
            AppMode.PROD -> prodUserRepository
            AppMode.TEST -> testUserRepository
            AppMode.STUB -> NoOpDbUserRepository
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
        return when(mode) {
            AppMode.PROD -> prodTTSClientRepository
            AppMode.TEST -> testTTSClientRepository
            AppMode.STUB -> NoOpTTSResourceRepository
        }
    }
}