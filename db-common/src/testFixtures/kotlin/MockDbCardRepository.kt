package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository

/**
 * Does not work with `io.mockk:mockk`
 * @see <a href='https://github.com/mockk/mockk/issues/288'>mockk#issue-288</a>
 */
class MockDbCardRepository(
    private val invokeGetCard: (CardId) -> CardEntityDbResponse = { CardEntityDbResponse.EMPTY },
    private val invokeGetAllCards: (DictionaryId) -> CardEntitiesDbResponse = { CardEntitiesDbResponse.EMPTY },
    private val invokeSearchCards: (CardFilter) -> CardEntitiesDbResponse = { CardEntitiesDbResponse.EMPTY },
    private val invokeCreateCard: (CardEntity) -> CardEntityDbResponse = { CardEntityDbResponse.EMPTY },
    private val invokeUpdateCard: (CardEntity) -> CardEntityDbResponse = { CardEntityDbResponse.EMPTY },
) : DbCardRepository {

    override fun getCard(id: CardId): CardEntityDbResponse {
        return invokeGetCard(id)
    }

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        return invokeGetAllCards(id)
    }

    override fun searchCard(filter: CardFilter): CardEntitiesDbResponse {
        return invokeSearchCards(filter)
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        return invokeCreateCard(card)
    }

    override fun updateCard(card: CardEntity): CardEntityDbResponse {
        return invokeUpdateCard(card)
    }
}