package com.gitlab.sszuev.flashcards.speaker.test

import com.gitlab.sszuev.flashcards.model.domain.ResourceGet
import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.repositories.ResourceEntityTTSResponse
import com.gitlab.sszuev.flashcards.repositories.ResourceIdTTSResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

object NullTTSResourceRepository : TTSResourceRepository {
    override suspend fun findResourceId(filter: ResourceGet): ResourceIdTTSResponse {
        return ResourceIdTTSResponse.EMPTY
    }

    override suspend fun getResource(id: ResourceId): ResourceEntityTTSResponse {
        return ResourceEntityTTSResponse.EMPTY
    }
}