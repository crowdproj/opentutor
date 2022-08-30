package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceId

object NoOpTTSResourceRepository: TTSResourceRepository {

    override suspend fun findResourceId(word: String, lang: LangId): ResourceId? {
        noOp()
    }

    override suspend fun getResource(id: ResourceId): ResourceEntity {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}