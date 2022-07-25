package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpDbCardRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpTTSResourceRepositoryImpl
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

data class CardRepositories(
    val ttsClient: TTSResourceRepository = NoOpTTSResourceRepositoryImpl,
    val cardRepository: DbCardRepository = NoOpDbCardRepository,
) {
    companion object {
        val DEFAULT = CardRepositories()
    }
}