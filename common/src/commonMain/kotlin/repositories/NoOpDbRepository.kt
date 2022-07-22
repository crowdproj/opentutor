package com.gitlab.sszuev.flashcards.repositories

object NoOpDbRepository : CardDbRepository {
    override fun getAllCards(dictionaryId: DictionaryIdDbRequest): CardEntitiesDbResponse {
        return noOp()
    }

    private fun <X> noOp(): X {
        throw IllegalStateException("Must not be called.")
    }
}