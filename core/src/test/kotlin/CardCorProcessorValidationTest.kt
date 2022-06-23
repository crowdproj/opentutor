package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.stubs.stubCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class CardCorProcessorValidationTest {

    companion object {
        private val processor = CardCorProcessor()
        private val requestId = UUID.randomUUID().toString()

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
    }

    @Test
    fun `test create-card - validate CardId`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = stubCard.copy(cardId = CardId(" "))
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-id", error)
    }

    @Test
    fun `test create-card - validate Card DictionaryId`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = stubCard.copy(dictionaryId = DictionaryId(" "))
        processor.execute(context)
        val error = error(context)
        assertValidationError("dictionary-id", error)
    }

    @Test
    fun `test create-card - validate Card Word`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = stubCard.copy(word = "")
        processor.execute(context)
        val error = error(context)
        assertValidationError("card-word", error)
    }

    @Test
    fun `test create-card - validate several fields`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD)
        context.requestCardEntity = stubCard.copy(dictionaryId = DictionaryId(""), word = "", cardId = CardId(""))
        processor.execute(context)
        val errors = context.errors
        Assertions.assertEquals(3, errors.size)
        assertValidationError("card-id", errors[0])
        assertValidationError("dictionary-id", errors[1])
        assertValidationError("card-word", errors[2])
    }
}