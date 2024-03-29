package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
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
    private val invokeGetCard: (AppUserId, CardId) -> CardDbResponse = { _, _ -> CardDbResponse.EMPTY },
    private val invokeGetAllCards: (AppUserId, DictionaryId) -> CardsDbResponse = { _, _ -> CardsDbResponse.EMPTY },
    private val invokeSearchCards: (AppUserId, CardFilter) -> CardsDbResponse = { _, _ -> CardsDbResponse.EMPTY },
    private val invokeCreateCard: (AppUserId, CardEntity) -> CardDbResponse = { _, _ -> CardDbResponse.EMPTY },
    private val invokeUpdateCard: (AppUserId, CardEntity) -> CardDbResponse = { _, _ -> CardDbResponse.EMPTY },
    private val invokeUpdateCards: (AppUserId, Iterable<CardId>, (CardEntity) -> CardEntity) -> CardsDbResponse = { _, _, _ -> CardsDbResponse.EMPTY },
    private val invokeResetCard: (AppUserId, CardId) -> CardDbResponse = { _, _ -> CardDbResponse.EMPTY },
    private val invokeDeleteCard: (AppUserId, CardId) -> RemoveCardDbResponse = { _, _ -> RemoveCardDbResponse.EMPTY },
) : DbCardRepository {

    override fun getCard(userId: AppUserId, cardId: CardId): CardDbResponse {
        return invokeGetCard(userId, cardId)
    }

    override fun getAllCards(userId: AppUserId, dictionaryId: DictionaryId): CardsDbResponse {
        return invokeGetAllCards(userId, dictionaryId)
    }

    override fun searchCard(userId: AppUserId, filter: CardFilter): CardsDbResponse {
        return invokeSearchCards(userId, filter)
    }

    override fun createCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse {
        return invokeCreateCard(userId, cardEntity)
    }

    override fun updateCard(userId: AppUserId, cardEntity: CardEntity): CardDbResponse {
        return invokeUpdateCard(userId, cardEntity)
    }

    override fun updateCards(
        userId: AppUserId,
        cardIds: Iterable<CardId>,
        update: (CardEntity) -> CardEntity
    ): CardsDbResponse {
        return invokeUpdateCards(userId, cardIds, update)
    }

    override fun resetCard(userId: AppUserId, cardId: CardId): CardDbResponse {
        return invokeResetCard(userId, cardId)
    }

    override fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse {
        return invokeDeleteCard(userId, cardId)
    }
}