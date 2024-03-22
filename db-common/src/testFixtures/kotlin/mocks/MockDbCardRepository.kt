package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse

/**
 * Does not work with `io.mockk:mockk`
 * @see <a href='https://github.com/mockk/mockk/issues/288'>mockk#issue-288</a>
 */
class MockDbCardRepository(
    private val invokeFindCard: (CardId) -> CardEntity? = { null },
    private val invokeFindCardsByDictionaryId: (DictionaryId) -> Sequence<CardEntity> = { emptySequence() },
    private val invokeFindCardsByDictionaryIds: (Iterable<DictionaryId>) -> Sequence<CardEntity> = { emptySequence() },
    private val invokeCreateCard: (CardEntity) -> CardEntity = { CardEntity.EMPTY },
    private val invokeUpdateCard: (AppUserId, CardEntity) -> CardDbResponse = { _, _ -> CardDbResponse.EMPTY },
    private val invokeUpdateCards: (AppUserId, Iterable<CardId>, (CardEntity) -> CardEntity) -> CardsDbResponse = { _, _, _ -> CardsDbResponse.EMPTY },
    private val invokeResetCard: (AppUserId, CardId) -> CardDbResponse = { _, _ -> CardDbResponse.EMPTY },
    private val invokeDeleteCard: (AppUserId, CardId) -> RemoveCardDbResponse = { _, _ -> RemoveCardDbResponse.EMPTY },
) : DbCardRepository {

    override fun findCard(cardId: CardId): CardEntity? = invokeFindCard(cardId)

    override fun findCards(dictionaryId: DictionaryId): Sequence<CardEntity> =
        invokeFindCardsByDictionaryId(dictionaryId)

    override fun findCards(dictionaryIds: Iterable<DictionaryId>): Sequence<CardEntity> =
        invokeFindCardsByDictionaryIds(dictionaryIds)

    override fun createCard(cardEntity: CardEntity): CardEntity = invokeCreateCard(cardEntity)

    override fun updateCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse =
        invokeUpdateCard(userId, cardEntity)

    override fun updateCards(
        userId: AppUserId,
        cardIds: Iterable<CardId>,
        update: (CardEntity) -> CardEntity
    ): CardsDbResponse = invokeUpdateCards(userId, cardIds, update)

    override fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse = invokeResetCard(userId, cardId)

    override fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse = invokeDeleteCard(userId, cardId)
}