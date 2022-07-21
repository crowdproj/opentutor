package com.gitlab.sszuev.flashcards.model.repositories

import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceId

object NoOpTTSResourceRepository: TTSResourceRepository {

    override suspend fun findResourceId(word: String, lang: LangId): ResourceId? {
        return null
    }

    override suspend fun getResource(id: ResourceId): ResourceEntity {
        throw IllegalStateException("Must not be called")
    }
}