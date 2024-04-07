package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.stubs.stubAudioResource
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.stubErrorForCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
internal class CardCorProcessorStubTest {

    companion object {
        private val processor = CardCorProcessor()
        private val requestId = UUID.randomUUID().toString()
        private val testCard = stubCard.copy()
        private val testAudioResourceGet = TTSResourceGet(lang = LangId("xx"), word = "xxx")

        private val testCardFilter = CardFilter(
            dictionaryIds = listOf(2, 4, 42).map { DictionaryId(it.toString()) },
            length = 42,
            random = true,
            onlyUnknown = false,
        )
        private val testCardLearn = CardLearn(
            cardId = CardId("42"),
            details = mapOf(Stage.SELF_TEST to 42)
        )

        private fun testContext(op: CardOperation, case: AppStub): CardContext {
            val context = CardContext(operation = op)
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

        @JvmStatic
        private fun updateCreateStubErrors(): List<Arguments> {
            val cases = listOf(
                AppStub.ERROR_CARD_WRONG_WORD,
                AppStub.ERROR_CARD_WRONG_TRANSCRIPTION,
                AppStub.ERROR_CARD_WRONG_TRANSLATION,
                AppStub.ERROR_CARD_WRONG_EXAMPLES,
                AppStub.ERROR_CARD_WRONG_PART_OF_SPEECH,
                AppStub.ERROR_CARD_WRONG_DETAILS,
                AppStub.ERROR_CARD_WRONG_AUDIO_RESOURCE,
            )
            val operations = listOf(CardOperation.CREATE_CARD, CardOperation.UPDATE_CARD)
            return operations.flatMap { op -> cases.map { Arguments.of(op, it) } }
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = CardOperation::class,
        names = [
            "CREATE_CARD",
            "UPDATE_CARD",
        ]
    )
    fun `test create-card & update-card success`(operation: CardOperation) = runTest {
        val context = testContext(operation, AppStub.SUCCESS)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(testCard, context.responseCardEntity)
    }

    @ParameterizedTest
    @EnumSource(
        value = CardOperation::class,
        names = [
            "CREATE_CARD",
            "UPDATE_CARD",
        ]
    )
    fun `test create-card & update-card unknown fail`(operation: CardOperation) = runTest {
        val context = testContext(operation, AppStub.UNKNOWN_ERROR)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertFail(context)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
    }

    @Test
    fun `test create-card specific fail`() =
        testCreateUpdateCardSpecificStubFail(CardOperation.CREATE_CARD, AppStub.ERROR_UNEXPECTED_FIELD)

    @Test
    fun `test update-card specific fail`() =
        testCreateUpdateCardSpecificStubFail(CardOperation.UPDATE_CARD, AppStub.ERROR_WRONG_CARD_ID)

    @ParameterizedTest
    @MethodSource("updateCreateStubErrors")
    fun `test create-card & update-card specific fail`(operation: CardOperation, case: AppStub) =
        testCreateUpdateCardSpecificStubFail(operation, case)

    private fun testCreateUpdateCardSpecificStubFail(operation: CardOperation, case: AppStub) = runTest {
        val context = testContext(operation, case)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertFail(context, stubErrorForCode(case))
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
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
    fun `test search-cards unknown fail`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS, AppStub.UNKNOWN_ERROR)
        context.requestCardFilter = testCardFilter
        processor.execute(context)
        assertFail(context)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
    }

    @ParameterizedTest
    @EnumSource(
        value = AppStub::class,
        names = [
            "ERROR_WRONG_DICTIONARY_ID",
            "ERROR_CARDS_WRONG_FILTER_LENGTH"
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
        val context = testContext(CardOperation.LEARN_CARDS, AppStub.SUCCESS)
        context.requestCardLearnList = listOf(testCardLearn, testCardLearn)
        processor.execute(context)
        assertSuccess(context)
    }

    @Test
    fun `test learn-cards unknown fail`() = runTest {
        val context = testContext(CardOperation.LEARN_CARDS, AppStub.UNKNOWN_ERROR)
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
        val context = testContext(CardOperation.LEARN_CARDS, case)
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

    @Test
    fun `test get-all-cards success`() = runTest {
        val context = testContext(CardOperation.GET_ALL_CARDS, AppStub.SUCCESS)
        context.requestDictionaryId = DictionaryId("42")
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(stubCards, context.responseCardEntityList)
    }

    @Test
    fun `test get-all-cards error`() = runTest {
        val context = testContext(CardOperation.GET_ALL_CARDS, AppStub.ERROR_WRONG_DICTIONARY_ID)
        context.requestDictionaryId = DictionaryId("42")
        processor.execute(context)
        assertFail(context, stubErrorForCode(AppStub.ERROR_WRONG_DICTIONARY_ID))
        Assertions.assertTrue(context.responseCardEntityList.isEmpty())
    }

    @ParameterizedTest
    @EnumSource(
        value = CardOperation::class,
        names = [
            "RESET_CARD",
            "DELETE_CARD",
        ]
    )
    fun `test request-with-card-id success`(operation: CardOperation) = runTest {
        val context = testContext(operation, AppStub.SUCCESS)
        context.requestCardEntityId = testCard.cardId
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
    }

    @ParameterizedTest
    @EnumSource(
        value = CardOperation::class,
        names = [
            "GET_CARD",
            "RESET_CARD",
            "DELETE_CARD",
        ]
    )
    fun `test request-with-card-id specific fail`(operation: CardOperation) = runTest {
        val context = testContext(operation, AppStub.ERROR_WRONG_CARD_ID)
        context.requestCardEntity = testCard
        processor.execute(context)
        assertFail(context, stubErrorForCode(AppStub.ERROR_WRONG_CARD_ID))
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
    }

    @Test
    fun `test get audio resource`() = runTest {
        val context = testContext(CardOperation.GET_RESOURCE, AppStub.SUCCESS)
        context.requestTTSResourceGet = testAudioResourceGet
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(stubAudioResource, context.responseTTSResourceEntity)
    }

    @ParameterizedTest
    @EnumSource(
        value = AppStub::class,
        names = [
            "ERROR_AUDIO_RESOURCE_WRONG_RESOURCE_ID",
            "ERROR_AUDIO_RESOURCE_SERVER_ERROR",
            "ERROR_AUDIO_RESOURCE_NOT_FOUND",
        ]
    )
    fun `test get-audion resource specific fail`(case: AppStub) = runTest {
        val context = testContext(CardOperation.GET_RESOURCE, case)
        context.requestTTSResourceGet = testAudioResourceGet
        processor.execute(context)
        assertFail(context, stubErrorForCode(case))
    }
}