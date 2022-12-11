package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
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

    override fun getCard(cardId: CardId): CardDbResponse {
        return invokeGetCard(cardId)
    }

    override fun getAllCards(dictionaryId: DictionaryId): CardsDbResponse {
        return invokeGetAllCards(dictionaryId)
    }

    override fun searchCard(filter: CardFilter): CardsDbResponse {
        return invokeSearchCards(filter)
    }

    override fun createCard(cardEntity: CardEntity): CardDbResponse {
        return invokeCreateCard(cardEntity)
    }

    override fun updateCard(cardEntity: CardEntity): CardDbResponse {
        return invokeUpdateCard(cardEntity)
    }

    override fun learnCards(cardLearn: List<CardLearn>): CardsDbResponse {
        return invokeLearnCards(cardLearn)
    }

    override fun resetCard(cardId: CardId): CardDbResponse {
        return invokeResetCard(cardId)
    }

    override fun deleteCard(cardId: CardId): DeleteCardDbResponse {
        return invokeDeleteCard(cardId)
    }
}