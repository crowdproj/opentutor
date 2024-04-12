package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.AppRepositories
import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceEntityResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceIdResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.MockTTSResourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class CardCorProcessorRunResourceTest {
    companion object {

        private fun testContext(repository: TTSResourceRepository): CardContext {
            val context = CardContext(
                operation = CardOperation.GET_RESOURCE,
                repositories = AppRepositories().copy(
                    ttsClientRepository = repository
                )
            )
            context.requestAppAuthId = AppAuthId("42")
            context.workMode = AppMode.TEST
            context.requestId = requestId()
            return context
        }

        private fun requestId(): AppRequestId {
            return AppRequestId("[for-${CardOperation.GET_RESOURCE}]")
        }
    }

    @Test
    fun `test get resource success`() = runTest {
        val testResourceGet = TTSResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceId = TTSResourceId("test-id")
        val testResourceEntity = ResourceEntity(
            resourceId = testResourceId,
            data = ByteArray(42) { 42 }
        )

        var findResourceIdWasCalled = false
        var getResourceWasCalled = false
        val repository = MockTTSResourceRepository(
            invokeFindResourceId = {
                findResourceIdWasCalled = true
                TTSResourceIdResponse(testResourceId)
            },
            invokeGetResource = {
                getResourceWasCalled = true
                TTSResourceEntityResponse(testResourceEntity)
            },
        )

        val context = testContext(repository)
        context.requestTTSResourceGet = testResourceGet

        CardCorProcessor().execute(context)

        Assertions.assertTrue(findResourceIdWasCalled)
        Assertions.assertTrue(getResourceWasCalled)
        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResourceEntity, context.responseTTSResourceEntity)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(1, repository.getResourceCounts.get())
    }

    @Test
    fun `test get resource fail no resource found`() = runTest {
        val testResourceGet = TTSResourceGet(word = "xxx", lang = LangId("en"))
        val testResourceId = TTSResourceId("test-id")
        val testResourceEntity = TTSResourceEntityResponse(ResourceEntity(
            resourceId = testResourceId,
            data = ByteArray(42) { 42 }
        ))

        var findResourceIdWasCalled = false
        var getResourceWasCalled = false
        val repository = MockTTSResourceRepository(
            invokeFindResourceId = {
                findResourceIdWasCalled = true
                TTSResourceIdResponse.EMPTY
            },
            invokeGetResource = {
                getResourceWasCalled = true
                testResourceEntity
            },
        )

        val context = testContext(repository)
        context.requestTTSResourceGet = testResourceGet

        CardCorProcessor().execute(context)

        Assertions.assertTrue(findResourceIdWasCalled)
        Assertions.assertFalse(getResourceWasCalled)
        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseTTSResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals(CardOperation.GET_RESOURCE.name, error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testResourceGet.toString(), error.field)
        Assertions.assertEquals("Error while GET_RESOURCE: no resource found. filter=${testResourceGet}", error.message)
        Assertions.assertNull(error.exception)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(0, repository.getResourceCounts.get())
    }

    @Test
    fun `test get resource fail exception`() = runTest {
        val testResourceGet = TTSResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceIdFound = TTSResourceIdResponse(TTSResourceId("test-id"))

        var findResourceIdWasCalled = false
        var getResourceWasCalled = false
        val repository = MockTTSResourceRepository(
            invokeFindResourceId = {
                findResourceIdWasCalled = true
                testResourceIdFound
            },
            invokeGetResource = {
                getResourceWasCalled = true
                throw TestException()
            },
        )

        val context = testContext(repository)
        context.requestTTSResourceGet = testResourceGet

        CardCorProcessor().execute(context)

        Assertions.assertTrue(findResourceIdWasCalled)
        Assertions.assertTrue(getResourceWasCalled)
        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseTTSResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals(CardOperation.GET_RESOURCE.name, error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testResourceGet.toString(), error.field)
        Assertions.assertEquals("Error while GET_RESOURCE: unexpected exception", error.message)
        Assertions.assertInstanceOf(TestException::class.java, error.exception)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(1, repository.getResourceCounts.get())
    }
}