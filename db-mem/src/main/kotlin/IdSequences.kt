package com.gitlab.sszuev.flashcards.dbmem

import java.util.concurrent.atomic.AtomicLong

internal class IdSequences(
    initUserId: Long = 0,
    initDictionaryId: Long = 0,
    initCardId: Long = 0,
) : IdGenerator {
    private val dictionarySequence = AtomicLong(initDictionaryId)
    private val cardSequence = AtomicLong(initCardId)
    private val userSequence = AtomicLong(initUserId)

    fun nextUserId(): Long {
        return userSequence.incrementAndGet()
    }

    override fun nextDictionaryId(): Long {
        return dictionarySequence.incrementAndGet()
    }

    override fun nextCardId(): Long {
        return cardSequence.incrementAndGet()
    }
}