package com.gitlab.sszuev.flashcards.speaker

interface ResourceCache {

    fun get(id: String): ByteArray?

    fun put(id: String, data: ByteArray)
}