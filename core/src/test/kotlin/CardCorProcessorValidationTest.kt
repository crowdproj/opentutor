package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class CardCorProcessorValidationTest {

    @OptIn(ExperimentalTime::class)
    companion object {
        private const val PARAMETERIZED_TEST_NAME =
            "test: ${ParameterizedInvocationConstants.INDEX_PLACEHOLDER}: \"${ParameterizedInvocationConstants.ARGUMENTS_WITH_NAMES_PLACEHOLDER}\""
        private val processor = CardCorProcessor()
        private val requestId = UUID.randomUUID().toString()
        private val testCard = testCardEntity1.copy()
        private val testCardFilter = CardFilter(
            dictionaryIds = listOf(4, 2, 42).map { DictionaryId(it.toString()) },
            length = 42,
            random = false,
            onlyUnknown = true,
        )
        private val testCardLearn = CardLearn(
            cardId = CardId("42"),
            details = mapOf(Stage.MOSAIC to 42)
        )

        private fun testContext(op: CardOperation): CardContext {
            val context = CardContext(operation = op)
            context.requestAppAuthId = AppAuthId("42")
            context.requestId = AppRequestId(requestId)
            return context
        }

        private fun error(context: CardContext): AppError {
            val errors = context.errors
            Assertions.assertEquals(1, errors.size) {
                "Got errors: ${errors.map { it.field }}"
            }
            return errors[0]
        }

        private fun assertValidationError(expectedField: String, actual: AppError) {
            Assertions.assertEquals("validators", actual.group)
            Assertions.assertEquals(expectedField, actual.field)
            Assertions.assertEquals("validation-$expectedField", actual.code)
        }

        @JvmStatic
        private fun wrongIds(): List<String> {
            return listOf(" ", "abc")
        }

        @JvmStatic
        private fun wrongWords(): List<String> {
            return listOf("", "x".repeat(42_000))
        }

        @JvmStatic
        private fun goodIds(): List<String> {
            return listOf("21", "42")
        }

        @JvmStatic
        private fun wrongIdsToOperationsWithCardIdInRequest(): List<Arguments> {
            val ops = listOf(CardOperation.GET_CARD, CardOperation.RESET_CARD, CardOperation.DELETE_CARD)
            val ids = wrongIds()
            return ops.flatMap { op -> ids.map { Arguments.of(it, op) } }
        }

        @JvmStatic
        private fun wrongIdsToCreateUpdateCardRequest(): List<Arguments> {
            val ops = listOf(CardOperation.CREATE_CARD, CardOperation.UPDATE_CARD)
            val ids = wrongIds()
            return ops.flatMap { op -> ids.map { Arguments.of(it, op) } }
        }

        @JvmStatic
        private fun wrongWordsCreateUpdateCardRequest(): List<Arguments> {
            val ops = listOf(CardOperation.CREATE_CARD, CardOperation.UPDATE_CARD)
            val words = wrongWords()
            return ops.flatMap { op -> words.map { Arguments.of(it, op) } }
        }
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource("goodIds")
    fun `test create-card - validate CardId`(id: String) = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testCard.copy(cardId = CardId(id))
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-id", error)
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource("wrongIds")
    fun `test update-card - validate CardId`(id: String) = runTest {
        val context = testContext(CardOperation.UPDATE_CARD)
        context.requestCardEntity = testCard.copy(cardId = CardId(id))
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-id", error)
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource("wrongIdsToCreateUpdateCardRequest")
    fun `test create-card & update-card - validate DictionaryId`(id: String, operation: CardOperation) = runTest {
        val context = testContext(operation)
        val cardId = if (operation == CardOperation.CREATE_CARD) CardId.NONE else CardId("42")
        context.requestCardEntity = testCard.copy(dictionaryId = DictionaryId(id), cardId = cardId)
        processor.execute(context)
        val error = error(context)
        assertValidationError("dictionary-id", error)
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource(value = ["wrongWordsCreateUpdateCardRequest"])
    fun `test create-card & update-card - validate word`(word: String, operation: CardOperation) =
        runTest {
            val context = testContext(operation)
            val cardId = if (operation == CardOperation.CREATE_CARD) CardId.NONE else CardId("42")
            context.requestCardEntity = testCard.copy(words = listOf(CardWordEntity(word = word)), cardId = cardId)
            processor.execute(context)
            val error = error(context)
            assertValidationError("card-word", error)
        }

    @Test
    fun `test update-card - validate wrong several fields`() = runTest {
        val context = testContext(CardOperation.UPDATE_CARD)
        context.requestCardEntity = testCard.copy(
            dictionaryId = DictionaryId(""),
            words = listOf(CardWordEntity(word = "")),
            cardId = CardId("xxxx")
        )
        processor.execute(context)
        val errors = context.errors
        Assertions.assertEquals(3, errors.size)
        assertValidationError("card-id", errors[0])
        assertValidationError("dictionary-id", errors[1])
        assertValidationError("card-word", errors[2])
    }

    @Test
    fun `test create-card - validate wrong several fields`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testCard.copy(
            dictionaryId = DictionaryId("sss"),
            words = listOf(CardWordEntity(word = "")),
            cardId = CardId("42")
        )
        processor.execute(context)
        val errors = context.errors
        Assertions.assertEquals(3, errors.size)
        assertValidationError("card-id", errors[0])
        assertValidationError("dictionary-id", errors[1])
        assertValidationError("card-word", errors[2])
    }

    @Test
    fun `test search-cards - validate card-filter length`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS)
        context.requestCardFilter = testCardFilter.copy(length = -42)
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-filter-length", error)
    }

    @Test
    fun `test search-cards - validate card-filter empty dictionaryIds`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS)
        context.requestCardFilter = testCardFilter.copy(dictionaryIds = emptyList())
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-filter-dictionary-ids", error)
    }

    @Test
    fun `test search-cards - validate card-filter wrong dictionaryIds`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS)
        context.requestCardFilter = testCardFilter.copy(dictionaryIds = wrongIds().map { DictionaryId(it) })
        processor.execute(context)
        val errors = context.errors
        Assertions.assertEquals(2, errors.size)
        errors.forEach {
            assertValidationError("card-filter-dictionary-ids", it)
        }
    }

    @Test
    fun `test search-cards - validate empty card-filter`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS)
        context.requestCardFilter = CardFilter()
        processor.execute(context)
        val errors = context.errors
        Assertions.assertEquals(2, errors.size)
        assertValidationError("card-filter-length", errors[0])
        assertValidationError("card-filter-dictionary-ids", errors[1])
    }

    @Test
    fun `test search-cards - validate wrong several fields`() = runTest {
        val context = testContext(CardOperation.SEARCH_CARDS)
        context.requestCardFilter = testCardFilter.copy(
            length = -42,
            dictionaryIds = wrongIds().map { DictionaryId(it) }
        )
        processor.execute(context)
        val errors = context.errors
        Assertions.assertEquals(3, errors.size)
        assertValidationError("card-filter-length", errors[0])
        assertValidationError("card-filter-dictionary-ids", errors[1])
        assertValidationError("card-filter-dictionary-ids", errors[2])
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource("wrongIds")
    fun `test learn-cards - validate CardIds`(id: String) = runTest {
        val context1 = testContext(CardOperation.LEARN_CARDS)
        context1.requestCardLearnList = listOf(
            testCardLearn.copy(cardId = CardId("1")),
            testCardLearn.copy(),
            testCardLearn.copy(cardId = CardId(id)),
        )
        processor.execute(context1)
        val error = error(context1)
        assertValidationError("card-learn-card-ids", error)

        val context2 = testContext(CardOperation.LEARN_CARDS)
        context2.requestCardLearnList = listOf(
            testCardLearn.copy(cardId = CardId(id)),
            testCardLearn.copy(cardId = CardId(id)),
        )
        processor.execute(context2)
        Assertions.assertEquals(2, context2.errors.size)
        assertValidationError("card-learn-card-ids", context2.errors[0])
        assertValidationError("card-learn-card-ids", context2.errors[1])
    }

    @Test
    fun `test learn-cards - validate wrong stages`() = runTest {
        val context = testContext(CardOperation.LEARN_CARDS)
        context.requestCardLearnList = listOf(
            testCardLearn.copy(cardId = CardId("42"), details = emptyMap()),
            testCardLearn.copy(cardId = CardId("21"), details = emptyMap()),
        )
        processor.execute(context)
        Assertions.assertEquals(2, context.errors.size)
        context.errors.forEach {
            assertValidationError("card-learn-stages", it)
        }
    }

    @Test
    fun `test learn-cards - validate wrong details`() = runTest {
        val context = testContext(CardOperation.LEARN_CARDS)
        context.requestCardLearnList = listOf(
            testCardLearn.copy(cardId = CardId("42"), details = mapOf(Stage.OPTIONS to 4200, Stage.WRITING to 42)),
            testCardLearn.copy(cardId = CardId("21"), details = mapOf(Stage.MOSAIC to -4200, Stage.SELF_TEST to -4200)),
        )
        processor.execute(context)
        Assertions.assertEquals(3, context.errors.size)
        context.errors.forEach {
            assertValidationError("card-learn-details", it)
        }
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource(value = ["wrongIdsToOperationsWithCardIdInRequest"])
    fun `test request-with-cardId - validate CardId`(id: String, op: CardOperation) = runTest {
        val context = testContext(op)
        context.requestCardEntityId = CardId(id)
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-id", error)
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @MethodSource(value = ["wrongIds"])
    fun `test get-all-cards - validate DictionaryId`(id: String) = runTest {
        val context = testContext(CardOperation.GET_ALL_CARDS)
        context.requestDictionaryId = DictionaryId(id)
        processor.execute(context)
        val error = error(context)
        assertValidationError("dictionary-id", error)
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @EnumSource(value = CardOperation::class, names = ["NONE"], mode = EnumSource.Mode.EXCLUDE)
    fun `test get-user - validate uid`(operation: CardOperation) = runTest {
        val context = testContext(operation)
            .copy(
                requestAppAuthId = AppAuthId(""),
                requestCardLearnList = listOf(CardLearn(CardId("42"), details = mapOf(Stage.MOSAIC to 42))),
                requestCardFilter = CardFilter(dictionaryIds = listOf(DictionaryId("42")), length = 42),
                requestCardEntityId = CardId("42"),
                requestDictionaryId = DictionaryId("42"),
                requestCardEntity = CardEntity(
                    cardId = if (operation == CardOperation.UPDATE_CARD) CardId("42") else CardId.NONE,
                    dictionaryId = DictionaryId("42"),
                    words = listOf(
                        CardWordEntity(
                            word = "xxx",
                            translations = listOf(listOf("kkk"))
                        )
                    )
                )
            )
        processor.execute(context)
        val error = error(context)
        assertValidationError("user-uid", error)
    }
}