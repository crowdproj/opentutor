package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.MockTTSResourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class TTSCorProcessorRunTest {
    companion object {

        private fun testContext(repository: TTSResourceRepository): TTSContext {
            val context = TTSContext(
                operation = TTSOperation.GET_RESOURCE,
                repository = repository
            )
            context.requestAppAuthId = AppAuthId("42")
            context.requestId = requestId()
            return context
        }

        private fun requestId(): AppRequestId {
            return AppRequestId("[for-${TTSOperation.GET_RESOURCE}]")
        }
    }

    @Test
    fun `test get resource success`() = runTest {
        val testResourceGet = TTSResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceEntity = ResourceEntity(
            resourceId = TTSResourceId("en:xxx"),
            data = ByteArray(42) { 42 }
        )

        var findResourceWasCalled = false
        val repository = MockTTSResourceRepository(
            invokeFindResource = { _, _ ->
                findResourceWasCalled = true
                testResourceEntity.data
            },
        )

        val context = testContext(repository)
        context.requestTTSResourceGet = testResourceGet

        TTSCorProcessor().execute(context)

        Assertions.assertTrue(findResourceWasCalled)
        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResourceEntity, context.responseTTSResourceEntity)

        Assertions.assertEquals(1, repository.findResourceCounts.get())
    }

    @Test
    fun `test get resource fail no resource found`() = runTest {
        val testResourceGet = TTSResourceGet(word = "xxx", lang = LangId("en"))

        var findResourceWasCalled = false
        val repository = MockTTSResourceRepository(
            invokeFindResource = { _, _ ->
                findResourceWasCalled = true
                null
            },
        )

        val context = testContext(repository)
        context.requestTTSResourceGet = testResourceGet

        TTSCorProcessor().execute(context)

        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertTrue(findResourceWasCalled)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseTTSResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals(TTSOperation.GET_RESOURCE.name, error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals("en:xxx", error.field)
        Assertions.assertEquals(
            "Error while GET_RESOURCE: no resource found. filter=${testResourceGet}",
            error.message
        )
        Assertions.assertNull(error.exception)

        Assertions.assertEquals(1, repository.findResourceCounts.get())
    }

    @Test
    fun `test get resource fail exception`() = runTest {
        val testResourceGet = TTSResourceGet(word = "xxx", lang = LangId("EN"))

        var findResourceWasCalled = false
        val repository = MockTTSResourceRepository(
            invokeFindResource = { _, _ ->
                findResourceWasCalled = true
                throw TestException()
            },
        )

        val context = testContext(repository)
        context.requestTTSResourceGet = testResourceGet

        TTSCorProcessor().execute(context)

        Assertions.assertTrue(findResourceWasCalled)
        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseTTSResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals(TTSOperation.GET_RESOURCE.name, error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testResourceGet.toString(), error.field)
        Assertions.assertEquals("Error while GET_RESOURCE: unexpected exception", error.message)
        Assertions.assertInstanceOf(TestException::class.java, error.exception)

        Assertions.assertEquals(1, repository.findResourceCounts.get())
    }
}