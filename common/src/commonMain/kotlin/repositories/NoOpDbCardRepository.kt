package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.*

object NoOpDbCardRepository : DbCardRepository {
    override fun getCard(id: CardId): CardEntityDbResponse {
        return noOp()
    }

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        return noOp()
    }

    override fun searchCard(filter: CardFilter): CardEntitiesDbResponse {
        return noOp()
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        return noOp()
    }

    override fun updateCard(card: CardEntity): CardEntityDbResponse {
        return noOp()
    }

    override fun learnCards(learn: List<CardLearn>): CardEntitiesDbResponse {
        return noOp()
    }

    override fun resetCard(id: CardId): CardEntityDbResponse {
        return noOp()
    }

    override fun deleteCard(id: CardId): DeleteEntityDbResponse {
        return noOp()
    }

    private fun <X> noOp(): X {
        error("Must not be called.")
    }
}