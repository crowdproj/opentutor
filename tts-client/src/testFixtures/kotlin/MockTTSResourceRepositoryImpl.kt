package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import java.util.concurrent.atomic.AtomicLong

/**
 * Does not work with `io.mockk:mockk`
 * @see <a href='https://github.com/mockk/mockk/issues/288'>mockk#issue-288</a>
 */
class MockTTSResourceRepositoryImpl(
    val answerResourceId: () -> ResourceId? = { ResourceId.NONE },
    val answerResourceEntity: () -> ResourceEntity = { ResourceEntity.DUMMY },
    val findResourceIdCounts: AtomicLong = AtomicLong(),
    val getResourceCounts: AtomicLong = AtomicLong(),
) : TTSResourceRepository {

    override suspend fun findResourceId(word: String, lang: LangId): ResourceId? {
        findResourceIdCounts.incrementAndGet()
        return answerResourceId()
    }

    override suspend fun getResource(id: ResourceId): ResourceEntity {
        getResourceCounts.incrementAndGet()
        return answerResourceEntity()
    }
}