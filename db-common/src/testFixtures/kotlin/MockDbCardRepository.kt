package com.gitlab.sszuev.flashcards.dbcommon

import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryIdDbRequest

/**
 * Does not work with `io.mockk:mockk`
 * @see <a href='https://github.com/mockk/mockk/issues/288'>mockk#issue-288</a>
 */
class MockDbCardRepository(
    private val invokeGetAllCards: (DictionaryIdDbRequest) -> CardEntitiesDbResponse = { CardEntitiesDbResponse.EMPTY },
) : DbCardRepository {

    override fun getAllCards(request: DictionaryIdDbRequest): CardEntitiesDbResponse {
        return invokeGetAllCards(request)
    }
}