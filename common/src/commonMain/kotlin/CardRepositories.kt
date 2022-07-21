package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.repositories.NoOpTTSResourceRepository
import com.gitlab.sszuev.flashcards.model.repositories.TTSResourceRepository

data class CardRepositories(
    val ttsClient: TTSResourceRepository = NoOpTTSResourceRepository,
) {
    companion object {
        val DEFAULT = CardRepositories()
    }
}