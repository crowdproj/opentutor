package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class CardCorProcessorStubsTest {

    companion object {
        private val processor = CardCorProcessor()
        private val requestId = UUID.randomUUID().toString()

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

        private fun assertFail(context: CardContext) {
            Assertions.assertEquals(requestId, context.requestId.asString())
            Assertions.assertEquals(AppStatus.FAIL, context.status)
            Assertions.assertEquals(listOf(stubError),  context.errors)
        }
    }

    @Test
    fun `test create-card success`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD, AppStub.SUCCESS)
        context.requestCardEntity = stubCard
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(stubCard, context.responseCardEntity)
    }

    @Test
    fun `test create-card fail`() = runTest {
        val context = testContext(CardOperation.CREATE_CARD, AppStub.UNKNOWN_ERROR)
        context.requestCardEntity = stubCard
        processor.execute(context)
        assertFail(context)
        Assertions.assertEquals(CardEntity.DUMMY, context.responseCardEntity)
    }
}