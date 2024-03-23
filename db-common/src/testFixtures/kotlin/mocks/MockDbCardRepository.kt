package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse

/**
 * Does not work with `io.mockk:mockk`
 * @see <a href='https://github.com/mockk/mockk/issues/288'>mockk#issue-288</a>
 */
class MockDbCardRepository(
    private val invokeFindCardById: (CardId) -> CardEntity? = { null },
    private val invokeFindCardsByDictionaryId: (DictionaryId) -> Sequence<CardEntity> = { emptySequence() },
    private val invokeFindCardsByDictionaryIdIn: (Iterable<DictionaryId>) -> Sequence<CardEntity> = { emptySequence() },
    private val invokeFindCardsByIdIn: (Iterable<CardId>) -> Sequence<CardEntity> = { emptySequence() },
    private val invokeCreateCard: (CardEntity) -> CardEntity = { CardEntity.EMPTY },
    private val invokeUpdateCard: (CardEntity) -> CardEntity = { CardEntity.EMPTY },
    private val invokeUpdateCards: (Iterable<CardEntity>) -> List<CardEntity> = { _ -> emptyList() },
    private val invokeResetCard: (AppUserId, CardId) -> CardDbResponse = { _, _ -> CardDbResponse.EMPTY },
    private val invokeDeleteCard: (AppUserId, CardId) -> RemoveCardDbResponse = { _, _ -> RemoveCardDbResponse.EMPTY },
) : DbCardRepository {

    override fun findCardById(cardId: CardId): CardEntity? = invokeFindCardById(cardId)

    override fun findCardsByDictionaryId(dictionaryId: DictionaryId): Sequence<CardEntity> =
        invokeFindCardsByDictionaryId(dictionaryId)

    override fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<DictionaryId>): Sequence<CardEntity> =
        invokeFindCardsByDictionaryIdIn(dictionaryIds)

    override fun findCardsByIdIn(cardIds: Iterable<CardId>): Sequence<CardEntity> = invokeFindCardsByIdIn(cardIds)

    override fun createCard(cardEntity: CardEntity): CardEntity = invokeCreateCard(cardEntity)

    override fun updateCard(cardEntity: CardEntity): CardEntity = invokeUpdateCard(cardEntity)

    override fun updateCards(cardEntities: Iterable<CardEntity>): List<CardEntity> = invokeUpdateCards(cardEntities)

    override fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse = invokeResetCard(userId, cardId)

    override fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse = invokeDeleteCard(userId, cardId)
}