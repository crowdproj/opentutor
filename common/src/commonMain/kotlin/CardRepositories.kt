package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.repositories.CardDbRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpCardDbRepository
import com.gitlab.sszuev.flashcards.repositories.NoOpTTSResourceRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

data class CardRepositories(
    val ttsClient: TTSResourceRepository = NoOpTTSResourceRepository,
    val cardRepository: CardDbRepository = NoOpCardDbRepository,
) {
    companion object {
        val DEFAULT = CardRepositories()
    }
}