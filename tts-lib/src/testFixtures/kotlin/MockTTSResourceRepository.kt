package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import java.util.concurrent.atomic.AtomicLong

class MockTTSResourceRepository(
    val findResourceCounts: AtomicLong = AtomicLong(),
    val invokeFindResource: (String, String) -> ByteArray? = { _, _ -> null },
) : TTSResourceRepository {

    override suspend fun findResource(lang: String, word: String): ByteArray? {
        findResourceCounts.incrementAndGet()
        return invokeFindResource(lang, word)
    }
}