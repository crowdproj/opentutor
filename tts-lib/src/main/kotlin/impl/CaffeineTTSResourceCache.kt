package com.gitlab.sszuev.flashcards.speaker.impl

import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.sszuev.flashcards.speaker.TTSResourceCache

class CaffeineTTSResourceCache : TTSResourceCache {
    private val cache = Caffeine.newBuilder().maximumSize(1024).build<String, ByteArray>()

    override fun get(id: String): ByteArray? = cache.getIfPresent(id)

    override fun put(id: String, data: ByteArray) = cache.put(id, data)
}