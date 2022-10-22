package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteCardDbResponse

/**
 * Does not work with `io.mockk:mockk`
 * @see <a href='https://github.com/mockk/mockk/issues/288'>mockk#issue-288</a>
 */
class MockDbCardRepository(
    private val invokeGetCard: (CardId) -> CardDbResponse = { CardDbResponse.EMPTY },
    private val invokeGetAllCards: (DictionaryId) -> CardsDbResponse = { CardsDbResponse.EMPTY },
    private val invokeSearchCards: (CardFilter) -> CardsDbResponse = { CardsDbResponse.EMPTY },
    private val invokeCreateCard: (CardEntity) -> CardDbResponse = { CardDbResponse.EMPTY },
    private val invokeUpdateCard: (CardEntity) -> CardDbResponse = { CardDbResponse.EMPTY },
    private val invokeLearnCards: (List<CardLearn>) -> CardsDbResponse = { CardsDbResponse.EMPTY },
    private val invokeResetCard: (CardId) -> CardDbResponse = { CardDbResponse.EMPTY },
    private val invokeDeleteCard: (CardId) -> DeleteCardDbResponse = { DeleteCardDbResponse.EMPTY },
) : DbCardRepository {

    override fun getCard(id: CardId): CardDbResponse {
        return invokeGetCard(id)
    }

    override fun getAllCards(id: DictionaryId): CardsDbResponse {
        return invokeGetAllCards(id)
    }

    override fun searchCard(filter: CardFilter): CardsDbResponse {
        return invokeSearchCards(filter)
    }

    override fun createCard(card: CardEntity): CardDbResponse {
        return invokeCreateCard(card)
    }

    override fun updateCard(card: CardEntity): CardDbResponse {
        return invokeUpdateCard(card)
    }

    override fun learnCards(learn: List<CardLearn>): CardsDbResponse {
        return invokeLearnCards(learn)
    }

    override fun resetCard(id: CardId): CardDbResponse {
        return invokeResetCard(id)
    }

    override fun deleteCard(id: CardId): DeleteCardDbResponse {
        return invokeDeleteCard(id)
    }
}