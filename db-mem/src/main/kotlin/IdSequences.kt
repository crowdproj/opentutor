package com.gitlab.sszuev.flashcards.dbmem

import java.util.concurrent.atomic.AtomicLong

internal class IdSequences(
    initDictionaryId: Long = 0,
    initCardId: Long = 0,
) : IdGenerator {
    private val dictionarySequence = AtomicLong(initDictionaryId)
    private val cardSequence = AtomicLong(initCardId)

    override fun nextDictionaryId(): Long {
        return dictionarySequence.incrementAndGet()
    }

    override fun nextCardId(): Long {
        return cardSequence.incrementAndGet()
    }
}