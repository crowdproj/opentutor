package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.speaker.MockTTSResourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class CardCorProcessorRunGetResourceTest {
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
    fun `test get resource success`() = runTest {
        val testResourceGet = ResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceId = ResourceId("test-id")
        val testResourceEntity = ResourceEntity(
            resourceId = testResourceId,
            data = ByteArray(42) { 42 }
        )

        val repository = MockTTSResourceRepository(
            answerResourceId = { testResourceId },
            answerResourceEntity = { testResourceEntity },
        )

        val context = testContext(CardOperation.GET_RESOURCE)
        context.requestResourceGet = testResourceGet

        CardCorProcessor(context.repositories.copy(ttsClient = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_RESOURCE), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResourceEntity, context.responseResourceEntity)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(1, repository.getResourceCounts.get())
    }

    @Test
    fun `test get resource fail no resource found`() = runTest {
        val testResourceGet = ResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceId = ResourceId("test-id")
        val testResourceEntity = ResourceEntity(
            resourceId = testResourceId,
            data = ByteArray(42) { 42 }
        )

        val repository = MockTTSResourceRepository(
            answerResourceId = { null },
            answerResourceEntity = { testResourceEntity },
        )

        val context = testContext(CardOperation.GET_RESOURCE)
        context.repositories = context.repositories.copy(ttsClient = repository)
        context.requestResourceGet = testResourceGet

        CardCorProcessor(context.repositories.copy(ttsClient = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_RESOURCE), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals("run::${CardOperation.GET_RESOURCE}", error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testResourceGet.toString(), error.field)
        Assertions.assertEquals("Error while GET_RESOURCE: no resource found", error.message)
        Assertions.assertNull(error.exception)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(0, repository.getResourceCounts.get())
    }

    @Test
    fun `test get resource fail exception`() = runTest {
        val testResourceGet = ResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceId = ResourceId("test-id")

        val repository = MockTTSResourceRepository(
            answerResourceId = { testResourceId },
            answerResourceEntity = { throw TestException() },
        )

        val context = testContext(CardOperation.GET_RESOURCE)
        context.repositories = context.repositories.copy(ttsClient = repository)
        context.requestResourceGet = testResourceGet

        CardCorProcessor(context.repositories.copy(ttsClient = repository)).execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_RESOURCE), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals("run::${CardOperation.GET_RESOURCE}", error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testResourceGet.toString(), error.field)
        Assertions.assertEquals("Error while GET_RESOURCE: exception", error.message)
        Assertions.assertInstanceOf(TestException::class.java, error.exception)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(1, repository.getResourceCounts.get())
    }

    private class TestException : RuntimeException()
}