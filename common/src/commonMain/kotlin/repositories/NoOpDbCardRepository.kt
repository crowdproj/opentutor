package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

object NoOpDbCardRepository : DbCardRepository {

    override fun findCardById(cardId: CardId): CardEntity = noOp()

    override fun findCardsByDictionaryId(dictionaryId: DictionaryId): Sequence<CardEntity> = noOp()

    override fun createCard(cardEntity: CardEntity): CardEntity = noOp()

    override fun updateCard(cardEntity: CardEntity): CardEntity = noOp()

    override fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse = noOp()

    override fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}