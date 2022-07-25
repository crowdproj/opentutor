package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

object NoOpDbCardRepository : DbCardRepository {
    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        return noOp()
    }

    private fun <X> noOp(): X {
        throw IllegalStateException("Must not be called.")
    }
}