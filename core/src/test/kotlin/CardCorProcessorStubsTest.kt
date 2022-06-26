package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.stubErrorForCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class CardCorProcessorStubsTest {

    companion object {
        private val processor = CardCorProcessor()
        private val requestId = UUID.randomUUID().toString()
        private val testCard = stubCard.copy()
        private val testCardFilter = CardFilter(
            dictionaryIds = listOf(2, 4, 42).map { DictionaryId(it.toString()) },
            length = 42,
            random = true,
            withUnknown = false,
        )
        private val testCardLearn = CardLearn(
            cardId = CardId("42"),
            details = mapOf("stage42" to 42)
        )

        private fun testContext(op: CardOperation, case: AppStub): CardContext {
            val context = CardContext()
            context.operation = op
            context.workMode = AppMode.STUB
            context.debugCase = case
            context.requestId = AppRequestId(requestId)
            return context
        }

        private fun assertSuccess(context: CardContext) {
            Assertions.assertEquals(requestId, context.requestId.asString())
            Assertions.assertEquals(AppStatus.OK, context.status)
            Assertions.assertTrue(context.errors.isEmpty())
        }

        private fun assertFail(context: CardContext, expected: AppError = stubError) {
            Assertions.assertEquals(requestId, context.requestId.asString())
            Assertions.assertEquals(AppStatus.FAIL, context.status)
            Assertions.assertEquals(listOf(expected), context.errors)
        }
    }

    @Test
    fun `test create-card success`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD, AppStub.SUCCESS)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(testCard, context.responseCardEntity)
    }

    @Test
    fun `test create-card fail`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD, AppStub.UNKNOWN_ERROR)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertFail(context)
        Assertions.assertEquals(CardEntity.DUMMY, context.responseCardEntity)
    }

    @ParameterizedTest
    @EnumSource(
        value = AppStub::class,
        names = [
            "ERROR_WRONG_CARD_ID",
            "ERROR_CARD_WRONG_WORD",
            "ERROR_CARD_WRONG_TRANSCRIPTION",
            "ERROR_CARD_WRONG_TRANSLATION",
            "ERROR_CARD_WRONG_EXAMPLES",
            "ERROR_CARD_WRONG_PART_OF_SPEECH",
            "ERROR_CARD_WRONG_DETAILS",
            "ERROR_CARD_WRONG_AUDIO_RESOURCE",
        ]
    )
    fun `test create-card specific fail`(case: AppStub) = runTest {
        val context = testContext(CardOperation.CREATE_CARD, case)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertFail(context, stubErrorForCode(case))
        Assertions.assertEquals(CardEntity.DUMMY, context.responseCardEntity)
    }

    @Test
    fun `test search-cards success`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS, AppStub.SUCCESS)
        context.requestCardFilter = testCardFilter
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(stubCards, context.responseCardEntityList)
    }

    @Test
    fun `test search-cards fail`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS, AppStub.UNKNOWN_ERROR)
        context.requestCardFilter = testCardFilter
        processor.execute(context)
        assertFail(context)
        Assertions.assertEquals(CardEntity.DUMMY, context.responseCardEntity)
    }

    @ParameterizedTest
    @EnumSource(
        value = AppStub::class,
        names = [
            "ERROR_CARDS_FILTER_WRONG_DICTIONARY_ID",
            "ERROR_CARDS_FILTER_WRONG_LENGTH"
        ]
    )
    fun `test search-cards specific fail`(case: AppStub) = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS, case)
        context.requestCardFilter = testCardFilter
        processor.execute(context)
        assertFail(context, stubErrorForCode(case))
        Assertions.assertTrue(context.responseCardEntityList.isEmpty())
    }

    @Test
    fun `test learn-cards success`() = runTest {
        val context = testContext(CardOperation.LEARN_CARD, AppStub.SUCCESS)
        context.requestCardLearnList = listOf(testCardLearn, testCardLearn)
        processor.execute(context)
        assertSuccess(context)
    }

    @Test
    fun `test learn-cards fail`() = runTest {
        val context = testContext(CardOperation.LEARN_CARD, AppStub.UNKNOWN_ERROR)
        context.requestCardLearnList = listOf(testCardLearn)
        processor.execute(context)
        assertFail(context)
    }

    @ParameterizedTest
    @EnumSource(
        value = AppStub::class,
        names = [
            "ERROR_LEARN_CARD_WRONG_CARD_ID",
            "ERROR_LEARN_CARD_WRONG_STAGES",
            "ERROR_LEARN_CARD_WRONG_DETAILS"
        ]
    )
    fun `test learn-cards specific fail`(case: AppStub) = runTest {
        val context = testContext(CardOperation.LEARN_CARD, case)
        context.requestCardLearnList = listOf(testCardLearn)
        processor.execute(context)
        assertFail(context, stubErrorForCode(case))
    }

    @Test
    fun `test get-card success`() = runTest {
        val context = testContext(CardOperation.GET_CARD, AppStub.SUCCESS)
        context.requestCardEntityId = testCard.cardId
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(stubCard, context.responseCardEntity)
    }

    @ParameterizedTest
    @EnumSource(
        value = CardOperation::class,
        names = [
            "GET_CARD",
        ]
    )
    fun `test get-card specific fail`(operation: CardOperation) = runTest {
        val context = testContext(operation, AppStub.ERROR_WRONG_CARD_ID)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertFail(context, stubErrorForCode(AppStub.ERROR_WRONG_CARD_ID))
        Assertions.assertEquals(CardEntity.DUMMY, context.responseCardEntity)
    }
}