package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceId

object NoOpTTSResourceRepository: TTSResourceRepository {

    override suspend fun findResourceId(word: String, lang: LangId): ResourceId? {
        return noOp()
    }

    override suspend fun getResource(id: ResourceId): ResourceEntity {
        return noOp()
    }

    private fun <X> noOp(): X {
        error("Must not be called.")
    }
}