package com.gitlab.sszuev.flashcards.common

interface IdGenerator {
    fun nextDictionaryId(): Long

    fun nextCardId(): Long
}