package com.gitlab.sszuev.flashcards.dbmem

interface IdGenerator {
    fun nextDictionaryId(): Long

    fun nextCardId(): Long
}