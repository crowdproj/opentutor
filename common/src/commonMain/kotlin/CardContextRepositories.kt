package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.model.repositories.DummyTTSResourceRepository
import com.gitlab.sszuev.flashcards.model.repositories.TTSResourceRepository

data class CardContextRepositories(
    val ttsClient: TTSResourceRepository = DummyTTSResourceRepository,
) {
    companion object {
        val DEFAULT = CardContextRepositories()
    }
}