package com.gitlab.sszuev.flashcards.speaker

interface TTSResourceCache {

    fun get(id: String): ByteArray?

    fun put(id: String, data: ByteArray)
}