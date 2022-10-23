package com.gitlab.sszuev.flashcards.common.documents

interface IdGenerator {
    fun nextDictionaryId(): Long

    fun nextCardId(): Long
}