package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.*

object NoOpDbCardRepository : DbCardRepository {
    override fun getCard(id: CardId): CardDbResponse {
        noOp()
    }

    override fun getAllCards(id: DictionaryId): CardsDbResponse {
        noOp()
    }

    override fun searchCard(filter: CardFilter): CardsDbResponse {
        noOp()
    }

    override fun createCard(card: CardEntity): CardDbResponse {
        noOp()
    }

    override fun updateCard(card: CardEntity): CardDbResponse {
        noOp()
    }

    override fun learnCards(learn: List<CardLearn>): CardsDbResponse {
        noOp()
    }

    override fun resetCard(id: CardId): CardDbResponse {
        noOp()
    }

    override fun deleteCard(id: CardId): DeleteCardDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}