package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceEntityResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceIdResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import java.util.concurrent.atomic.AtomicLong

class MockTTSResourceRepository(
    val findResourceIdCounts: AtomicLong = AtomicLong(),
    val getResourceCounts: AtomicLong = AtomicLong(),
    val invokeFindResourceId: (TTSResourceGet) -> TTSResourceIdResponse = { TTSResourceIdResponse.EMPTY },
    val invokeGetResource: (TTSResourceId) -> TTSResourceEntityResponse = { TTSResourceEntityResponse.EMPTY },
) : TTSResourceRepository {

    override suspend fun findResourceId(filter: TTSResourceGet): TTSResourceIdResponse {
        findResourceIdCounts.incrementAndGet()
        return invokeFindResourceId(filter)
    }

    override suspend fun getResource(id: TTSResourceId): TTSResourceEntityResponse {
        getResourceCounts.incrementAndGet()
        return invokeGetResource(id)
    }
}