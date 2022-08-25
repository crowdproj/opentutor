package com.gitlab.sszuev.flashcards.speaker.test

import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

object NullTTSResourceRepository : TTSResourceRepository {
    override suspend fun findResourceId(word: String, lang: LangId): ResourceId? {
        return null
    }

    override suspend fun getResource(id: ResourceId): ResourceEntity {
        return ResourceEntity.DUMMY
    }
}