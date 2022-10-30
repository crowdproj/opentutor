package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

object NoOpTTSResourceRepository: TTSResourceRepository {

    override suspend fun findResourceId(filter: TTSResourceGet): TTSResourceIdResponse {
        noOp()
    }

    override suspend fun getResource(id: TTSResourceId): TTSResourceEntityResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}