package com.gitlab.sszuev.flashcards.repositories

object NoOpDbRepository : CardDbRepository {
    override fun getAllCards(request: DictionaryIdDbRequest): CardEntitiesDbResponse {
        return noOp()
    }

    private fun <X> noOp(): X {
        throw IllegalStateException("Must not be called.")
    }
}