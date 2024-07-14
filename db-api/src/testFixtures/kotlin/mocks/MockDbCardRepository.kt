package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository

class MockDbCardRepository(
    private val invokeFindCardById: (String) -> DbCard? = { null },
    private val invokeFindCardsByDictionaryId: (String) -> Sequence<DbCard> = { emptySequence() },
    private val invokeFindCardsByDictionaryIdIn: (Iterable<String>) -> Sequence<DbCard> = { emptySequence() },
    private val invokeFindCardsByIdIn: (Iterable<String>) -> Sequence<DbCard> = { emptySequence() },
    private val invokeCreateCard: (DbCard) -> DbCard = { DbCard.NULL },
    private val invokeCreateCards: (Iterable<DbCard>) -> List<DbCard> = { emptyList() },
    private val invokeUpdateCard: (DbCard) -> DbCard = { DbCard.NULL },
    private val invokeUpdateCards: (Iterable<DbCard>) -> List<DbCard> = { emptyList() },
    private val invokeDeleteCard: (String) -> DbCard = { _ -> DbCard.NULL },
    private val invokeCountCardsByDictionaryIdIn: (Iterable<String>) -> Map<String, Long> = { emptyMap() },
    private val invokeCountAnsweredCardsByDictionaryIdIn: (Int, Iterable<String>) -> Map<String, Long> = { _, _ -> emptyMap() },
) : DbCardRepository {

    override fun findCardById(cardId: String): DbCard? = invokeFindCardById(cardId)

    override fun findCardsByDictionaryId(dictionaryId: String): Sequence<DbCard> =
        invokeFindCardsByDictionaryId(dictionaryId)

    override fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<String>): Sequence<DbCard> =
        invokeFindCardsByDictionaryIdIn(dictionaryIds)

    override fun findCardsByIdIn(cardIds: Iterable<String>): Sequence<DbCard> = invokeFindCardsByIdIn(cardIds)

    override fun createCard(cardEntity: DbCard): DbCard = invokeCreateCard(cardEntity)

    override fun createCards(cardEntities: Iterable<DbCard>): List<DbCard> = invokeCreateCards(cardEntities)

    override fun updateCard(cardEntity: DbCard): DbCard = invokeUpdateCard(cardEntity)

    override fun updateCards(cardEntities: Iterable<DbCard>): List<DbCard> = invokeUpdateCards(cardEntities)

    override fun deleteCard(cardId: String): DbCard = invokeDeleteCard(cardId)

    override fun countCardsByDictionaryId(dictionaryIds: Iterable<String>): Map<String, Long> =
        invokeCountCardsByDictionaryIdIn(dictionaryIds)

    override fun countCardsByDictionaryIdAndAnswered(
        dictionaryIds: Iterable<String>,
        greaterOrEqual: Int
    ): Map<String, Long> = invokeCountAnsweredCardsByDictionaryIdIn(greaterOrEqual, dictionaryIds)
}