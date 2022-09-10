package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.stubs.stubDictionaries
import com.gitlab.sszuev.flashcards.stubs.stubError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class DictionaryCorProcessorStubTest {
    companion object {
        private val processor = DictionaryCorProcessor()
        private val requestId = UUID.randomUUID().toString()
        private val testUser = AppUserEntity(AppUserId("42"), AppAuthId("xxx"))

        @Suppress("SameParameterValue")
        private fun testContext(op: DictionaryOperation, case: AppStub): DictionaryContext {
            val context = DictionaryContext(operation = op)
            context.workMode = AppMode.STUB
            context.debugCase = case
            context.requestId = AppRequestId(requestId)
            return context
        }

        private fun assertSuccess(context: DictionaryContext) {
            Assertions.assertEquals(requestId, context.requestId.asString())
            Assertions.assertEquals(AppStatus.OK, context.status)
            Assertions.assertTrue(context.errors.isEmpty())
        }

        private fun assertFail(context: DictionaryContext, expected: AppError = stubError) {
            Assertions.assertEquals(requestId, context.requestId.asString())
            Assertions.assertEquals(AppStatus.FAIL, context.status)
            Assertions.assertEquals(listOf(expected), context.errors)
        }
    }

    @Test
    fun `test get-all-dictionary success`() = runTest {
        val context = testContext(DictionaryOperation.GET_ALL_DICTIONARIES, AppStub.SUCCESS)
        context.contextUserEntity = testUser
        processor.execute(context)
        assertSuccess(context)
        Assertions.assertEquals(stubDictionaries, context.responseDictionaryEntityList)
    }

    @Test
    fun `test get-all-dictionaries error`() = runTest {
        val context = testContext(DictionaryOperation.GET_ALL_DICTIONARIES, AppStub.UNKNOWN_ERROR)
        context.contextUserEntity = testUser
        processor.execute(context)
        processor.execute(context)
        assertFail(context, stubError)
        Assertions.assertTrue(context.responseDictionaryEntityList.isEmpty())
    }
}