package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpTTSResourceRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

data class CardRepositories(
    private val prodTTSClientRepository: TTSResourceRepository = NoOpTTSResourceRepository,
    private val testTTSClientRepository: TTSResourceRepository = NoOpTTSResourceRepository,
    private val prodCardRepository: DbCardRepository = NoOpDbCardRepository,
    private val testCardRepository: DbCardRepository = NoOpDbCardRepository,
) {
    companion object {
        val DEFAULT = CardRepositories()
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