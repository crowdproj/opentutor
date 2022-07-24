package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.dbcommon.MockCardDbRepository
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.toDbRequest
import com.gitlab.sszuev.flashcards.stubs.stubCards
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("OPT_IN_USAGE")
internal class CardCorProcessorRunCardsTest {
    companion object {

        private fun testContext(op: CardOperation, mode: AppMode = AppMode.TEST): CardContext {
            val context = CardContext()
            context.operation = op
            context.workMode = mode
            context.requestId = requestId(op, mode)
            return context
        }

        private fun requestId(op: CardOperation, mode: AppMode = AppMode.TEST): AppRequestId {
            return AppRequestId("for-$op:$mode")
        }
    }

    @Test
    fun `test get-all-cards fail`() = runTest {
        val testDictionaryId = DictionaryId("42").toDbRequest()
        val testResponseEntities = stubCards

        val repository = MockCardDbRepository(
            invokeGetAllCards = {
                CardEntitiesDbResponse(if (it == testDictionaryId) testResponseEntities else emptyList())
            }
        )

        val context = testContext(CardOperation.GET_ALL_CARDS)
        context.requestDictionaryId = testDictionaryId.id

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test get-all-cards unexpected fail`() = runTest {
        val testDictionaryId = DictionaryId("42").toDbRequest()
        val testResponseEntities = stubCards

        val repository = MockCardDbRepository(
            invokeGetAllCards = {
                CardEntitiesDbResponse(
                    if (it != testDictionaryId)
                        testResponseEntities
                    else throw TestException()
                )
            }
        )

        val context = testContext(CardOperation.GET_ALL_CARDS)
        context.requestDictionaryId = testDictionaryId.id

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(0, context.responseCardEntityList.size)
        val error = context.errors[0]
        Assertions.assertEquals("run::${CardOperation.GET_ALL_CARDS}", error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testDictionaryId.id.asString(), error.field)
        Assertions.assertEquals("Error while GET_ALL_CARDS: exception", error.message)
        Assertions.assertInstanceOf(TestException::class.java, error.exception)
    }

    private class TestException : RuntimeException()
}