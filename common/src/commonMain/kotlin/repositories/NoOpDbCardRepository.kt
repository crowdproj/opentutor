package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

object NoOpDbCardRepository : DbCardRepository {

    override fun findCard(cardId: CardId): CardEntity = noOp()

    override fun getAllCards(userId: AppUserId, dictionaryId: DictionaryId): CardsDbResponse = noOp()

    override fun searchCard(userId: AppUserId, filter: CardFilter): CardsDbResponse = noOp()

    override fun createCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse = noOp()

    override fun updateCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse = noOp()

    override fun updateCards(
        userId: AppUserId,
        cardIds: Iterable<CardId>,
        update: (CardEntity) -> CardEntity
    ): CardsDbResponse = noOp()

    override fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse = noOp()

    override fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}