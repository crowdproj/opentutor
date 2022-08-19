package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

object NoOpDbCardRepository : DbCardRepository {
    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        return noOp()
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        return noOp()
    }

    override fun searchCard(filter: CardFilter): CardEntitiesDbResponse {
        return noOp()
    }

    private fun <X> noOp(): X {
        throw IllegalStateException("Must not be called.")
    }
}