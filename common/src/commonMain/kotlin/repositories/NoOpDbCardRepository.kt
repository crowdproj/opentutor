package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

object NoOpDbCardRepository : DbCardRepository {
    override fun getCard(cardId: CardId): CardDbResponse {
        noOp()
    }

    override fun getAllCards(dictionaryId: DictionaryId): CardsDbResponse {
        noOp()
    }

    override fun searchCard(filter: CardFilter): CardsDbResponse {
        noOp()
    }

    override fun createCard(cardEntity: CardEntity): CardDbResponse {
        noOp()
    }

    override fun updateCard(cardEntity: CardEntity): CardDbResponse {
        noOp()
    }

    override fun learnCards(cardLearn: List<CardLearn>): CardsDbResponse {
        noOp()
    }

    override fun resetCard(cardId: CardId): CardDbResponse {
        noOp()
    }

    override fun deleteCard(cardId: CardId): DeleteCardDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}