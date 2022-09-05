package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.ResourceGet
import com.gitlab.sszuev.flashcards.model.domain.ResourceId

object NoOpTTSResourceRepository: TTSResourceRepository {

    override suspend fun findResourceId(filter: ResourceGet): ResourceIdTTSResponse {
        noOp()
    }

    override suspend fun getResource(id: ResourceId): ResourceEntityTTSResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}