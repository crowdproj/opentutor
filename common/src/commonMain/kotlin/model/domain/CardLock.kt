package com.gitlab.sszuev.flashcards.model.domain

/**
 * Part of optimistic lock mechanism.
 */
@JvmInline
value class CardLock(private val id: String) {
    fun asString() = id

    companion object {
        val NONE = CardLock("")
    }
}