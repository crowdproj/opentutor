package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.stubs.stubCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class CardCorProcessorValidationTest {

    companion object {
        private val processor = CardCorProcessor()
        private val requestId = UUID.randomUUID().toString()
        private val testCard = stubCard.copy()
        private val testCardFilter = CardFilter(
            dictionaryIds = listOf(4, 2, 42).map { DictionaryId(it.toString()) },
            length = 42,
            random = false,
            withUnknown = true,
        )

        private fun testContext(op: CardOperation): CardContext {
            val context = CardContext()
            context.operation = op
            context.workMode = AppMode.TEST
            context.requestId = AppRequestId(requestId)
            return context
        }

        private fun error(context: CardContext): AppError {
            val errors = context.errors
            Assertions.assertEquals(1, errors.size)
            return errors[0]
        }

        private fun assertValidationError(expectedField: String, actual: AppError) {
            Assertions.assertEquals("validation", actual.group)
            Assertions.assertEquals(expectedField, actual.field)
            Assertions.assertEquals("validation-$expectedField", actual.code)
        }

        @JvmStatic
        private fun wrongIds(): List<String> {
            return listOf(" ", "abc")
        }
    }

    @ParameterizedTest
    @MethodSource("wrongIds")
    fun `test create-card - validate CardId`(id: String) = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testCard.copy(cardId = CardId(id))
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-id", error)
    }

    @ParameterizedTest
    @MethodSource("wrongIds")
    fun `test create-card - validate Card DictionaryId`(id: String) = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testCard.copy(dictionaryId = DictionaryId(id))
        processor.execute(context)
        val error = error(context)
        assertValidationError("dictionary-id", error)
    }

    @Test
    fun `test create-card - validate Card Word`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testCard.copy(word = "")
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-word", error)
    }

    @Test
    fun `test create-card - validate several fields`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = testCard.copy(dictionaryId = DictionaryId(""), word = "", cardId = CardId(""))
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
}