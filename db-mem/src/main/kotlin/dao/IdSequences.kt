package com.gitlab.sszuev.flashcards.dbmem.dao

import java.util.concurrent.atomic.AtomicLong

class IdSequences {
    private val dictionarySequence = AtomicLong()
    private val cardSequence = AtomicLong()
    private val exampleSequence = AtomicLong()
    private val translationSequence = AtomicLong()

    fun nextDictionaryId(): Long {
        return dictionarySequence.incrementAndGet()
    }

    fun nextCardId(): Long {
        return cardSequence.incrementAndGet()
    }

    fun nextExampleId(): Long {
        return exampleSequence.incrementAndGet()
    }

    fun nextTranslationId(): Long {
        return translationSequence.incrementAndGet()
    }
}