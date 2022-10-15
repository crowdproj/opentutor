package com.gitlab.sszuev.flashcards.dbmem

import java.util.concurrent.atomic.AtomicLong

class IdSequences {
    private val dictionarySequence = AtomicLong()
    private val cardSequence = AtomicLong()
    private val exampleSequence = AtomicLong()
    private val translationSequence = AtomicLong()
    private val userSequence = AtomicLong()

    fun nextUserId(): Long {
        return userSequence.incrementAndGet()
    }

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

    /**
     * Back door for testing
     */
    fun reset() {
        userSequence.set(0)
        dictionarySequence.set(0)
        cardSequence.set(0)
        exampleSequence.set(0)
        translationSequence.set(0)
    }

    companion object {
        /**
         * Global id registry.
         */
        val globalIdsGenerator: IdSequences = IdSequences()
    }
}