package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

object NoOpDbCardRepository : DbCardRepository {

    override fun findCardById(cardId: CardId): CardEntity = noOp()

    override fun findCardsByDictionaryId(dictionaryId: DictionaryId): Sequence<CardEntity> = noOp()

    override fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<DictionaryId>): Sequence<CardEntity> = noOp()

    override fun findCardsByIdIn(cardIds: Iterable<CardId>): Sequence<CardEntity> = noOp()

    override fun createCard(cardEntity: CardEntity): CardEntity = noOp()

    override fun updateCard(cardEntity: CardEntity): CardEntity = noOp()

    override fun updateCards(cardEntities: Iterable<CardEntity>): List<CardEntity> = noOp()

    override fun deleteCard(cardId: CardId): CardEntity = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}