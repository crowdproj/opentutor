package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.dbcommon.MockDbCardRepository
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.stubs.stubCard
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

        private fun assertError(context: CardContext, op: CardOperation) {
            Assertions.assertEquals(requestId(op), context.requestId)
            Assertions.assertEquals(AppStatus.FAIL, context.status)
            Assertions.assertEquals(1, context.errors.size)
            val error = context.errors[0]
            Assertions.assertEquals("run::$op", error.code)
            Assertions.assertEquals("run", error.group)
            Assertions.assertEquals("Error while $op: exception", error.message)
            Assertions.assertInstanceOf(TestException::class.java, error.exception)
        }

        private class TestException : RuntimeException()
    }

    @Test
    fun `test get-card success`() = runTest {
        val testId = CardId("42")
        val testResponseEntity = stubCard.copy(cardId = testId)

        val repository = MockDbCardRepository(
            invokeGetCard = {
                CardEntityDbResponse(if (it == testId) testResponseEntity else CardEntity.EMPTY)
            }
        )

        val context = testContext(CardOperation.GET_CARD)
        context.requestCardEntityId = testId

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test get-all-cards success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeGetAllCards = {
                CardEntitiesDbResponse(if (it == testDictionaryId) testResponseEntities else emptyList())
            }
        )

        val context = testContext(CardOperation.GET_ALL_CARDS)
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test get-all-cards unexpected fail`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeGetAllCards = {
                CardEntitiesDbResponse(
                    if (it != testDictionaryId)
                        testResponseEntities
                    else throw TestException()
                )
            }
        )

        val context = testContext(CardOperation.GET_ALL_CARDS)
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(0, context.responseCardEntityList.size)
        assertError(context, CardOperation.GET_ALL_CARDS)
    }

    @Test
    fun `test create-card success`() = runTest {
        val testResponseEntity = stubCard.copy(word = "HHH")
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = CardId.NONE)

        val repository = MockDbCardRepository(
            invokeCreateCard = {
                CardEntityDbResponse(if (it.word == testRequestEntity.word) testResponseEntity else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.CREATE_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status) { "Errors: ${context.errors}" }
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test create-card unexpected fail`() = runTest {
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = CardId.NONE)

        val repository = MockDbCardRepository(
            invokeCreateCard = {
                CardEntityDbResponse(if (it.word == testRequestEntity.word) throw TestException() else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.CREATE_CARD), context.requestId)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
        assertError(context, CardOperation.CREATE_CARD)
    }

    @Test
    fun `test search-cards success`() = runTest {
        val testFilter = CardFilter(
            dictionaryIds = listOf(DictionaryId("21"), DictionaryId("42")),
            random = true,
            withUnknown = true,
            length = 42,
        )
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeSearchCards = {
                CardEntitiesDbResponse(if (it == testFilter) testResponseEntities else emptyList())
            }
        )

        val context = testContext(CardOperation.SEARCH_CARDS)
        context.requestCardFilter = testFilter

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.SEARCH_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test search-cards unexpected fail`() = runTest {
        val testFilter = CardFilter(
            dictionaryIds = listOf(DictionaryId("42")),
            random = false,
            withUnknown = false,
            length = 1,
        )
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeSearchCards = {
                CardEntitiesDbResponse(
                    if (it != testFilter)
                        testResponseEntities
                    else throw TestException()
                )
            }
        )

        val context = testContext(CardOperation.SEARCH_CARDS)
        context.requestCardFilter = testFilter

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)
        Assertions.assertEquals(0, context.responseCardEntityList.size)
        assertError(context, CardOperation.SEARCH_CARDS)
    }

    @Test
    fun `test update-card success`() = runTest {
        val cardId = CardId("42")
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = cardId)
        val testResponseEntity = stubCard.copy(word = "HHH")

        val repository = MockDbCardRepository(
            invokeUpdateCard = {
                CardEntityDbResponse(if (it.cardId == cardId) testResponseEntity else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.UPDATE_CARD)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.UPDATE_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status) { "Errors: ${context.errors}" }
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test update-card unexpected fail`() = runTest {
        val cardId = CardId("42")
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = cardId)

        val repository = MockDbCardRepository(
            invokeUpdateCard = {
                CardEntityDbResponse(if (it.word == testRequestEntity.word) throw TestException() else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.UPDATE_CARD)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.UPDATE_CARD), context.requestId)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
        assertError(context, CardOperation.UPDATE_CARD)
    }

    @Test
    fun `test learn-cards success`() = runTest {
        val testLearn = listOf(
            CardLearn(cardId = stubCard.cardId, details = mapOf(Stage.WRITING to 42)),
        )

        val testResponseEntities = listOf(stubCard)

        val repository = MockDbCardRepository(
            invokeLearnCards = {
                CardEntitiesDbResponse(
                    cards = if (it == testLearn) testResponseEntities else emptyList(),
                )
            }
        )

        val context = testContext(CardOperation.LEARN_CARDS)
        context.requestCardLearnList = testLearn

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.LEARN_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test learn-cards error`() = runTest {
        val testLearn = listOf(
            CardLearn(cardId = CardId("1"), details = mapOf(Stage.SELF_TEST to 42)),
            CardLearn(cardId = CardId("2"), details = mapOf(Stage.OPTIONS to 2, Stage.MOSAIC to 3))
        )

        val testResponseEntities = stubCards
        val testResponseErrors = listOf(
            AppError(code = "test")
        )

        val repository = MockDbCardRepository(
            invokeLearnCards = {
                CardEntitiesDbResponse(
                    cards = if (it == testLearn) testResponseEntities else emptyList(),
                    errors = if (it == testLearn) testResponseErrors else emptyList()
                )
            }
        )

        val context = testContext(CardOperation.LEARN_CARDS)
        context.requestCardLearnList = testLearn

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.LEARN_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(testResponseErrors, context.errors)
        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test reset-card success`() = runTest {
        val testId = CardId("42")
        val testResponseEntity = stubCard.copy(cardId = testId)

        val repository = MockDbCardRepository(
            invokeResetCard = {
                CardEntityDbResponse(
                    card = if (it == testId) testResponseEntity else CardEntity.EMPTY,
                )
            }
        )

        val context = testContext(CardOperation.RESET_CARD)
        context.requestCardEntityId = testId

        CardCorProcessor(context.repositories.copy(cardRepository = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.RESET_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

}