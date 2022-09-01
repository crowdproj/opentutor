package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.*

object NoOpDbCardRepository : DbCardRepository {
    override fun getCard(id: CardId): CardEntityDbResponse {
        noOp()
    }

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        noOp()
    }

    override fun searchCard(filter: CardFilter): CardEntitiesDbResponse {
        noOp()
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        noOp()
    }

    override fun updateCard(card: CardEntity): CardEntityDbResponse {
        noOp()
    }

    override fun learnCards(learn: List<CardLearn>): CardEntitiesDbResponse {
        noOp()
    }

    override fun resetCard(id: CardId): CardEntityDbResponse {
        noOp()
    }

    override fun deleteCard(id: CardId): DeleteEntityDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}