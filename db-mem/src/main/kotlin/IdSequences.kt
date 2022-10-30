package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.documents.IdGenerator
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class IdSequences : IdGenerator {
    private val dictionarySequence = AtomicLong()
    private val cardSequence = AtomicLong()
    private val userSequence = AtomicLong()

    fun nextUserId(): Long {
        return userSequence.incrementAndGet()
    }

    override fun nextDictionaryId(): Long {
        return dictionarySequence.incrementAndGet()
    }

    override fun nextCardId(): Long {
        return cardSequence.incrementAndGet()
    }

    internal fun position(dictionary: MemDbDictionary) : IdSequences {
        dictionarySequence.set(max(dictionary.id, dictionarySequence.get()))
        cardSequence.set(max(dictionary.cards.keys.max(), cardSequence.get()))
        return this
    }
    /**
     * Back door for testing
     */
    fun reset() {
        userSequence.set(0)
        dictionarySequence.set(0)
        cardSequence.set(0)
    }

    companion object {
        /**
         * Global id registry.
         */
        val globalIdsGenerator: IdSequences = IdSequences()
    }
}