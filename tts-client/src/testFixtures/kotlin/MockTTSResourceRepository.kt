package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.ResourceGet
import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.repositories.ResourceEntityTTSResponse
import com.gitlab.sszuev.flashcards.repositories.ResourceIdTTSResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import java.util.concurrent.atomic.AtomicLong

/**
 * Does not work with `io.mockk:mockk`
 * @see <a href='https://github.com/mockk/mockk/issues/288'>mockk#issue-288</a>
 */
class MockTTSResourceRepository(
    val findResourceIdCounts: AtomicLong = AtomicLong(),
    val getResourceCounts: AtomicLong = AtomicLong(),
    val invokeFindResourceId: (ResourceGet) -> ResourceIdTTSResponse = { ResourceIdTTSResponse.EMPTY },
    val invokeGetResource: (ResourceId) -> ResourceEntityTTSResponse = { ResourceEntityTTSResponse.EMPTY },
) : TTSResourceRepository {

    override suspend fun findResourceId(filter: ResourceGet): ResourceIdTTSResponse {
        findResourceIdCounts.incrementAndGet()
        return invokeFindResourceId(filter)
    }

    override suspend fun getResource(id: ResourceId): ResourceEntityTTSResponse {
        getResourceCounts.incrementAndGet()
        return invokeGetResource(id)
    }
}