package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.ResourceEntityTTSResponse
import com.gitlab.sszuev.flashcards.repositories.ResourceIdTTSResponse
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
                repositories = CardRepositories().copy(
                    testUserRepository = MockDbUserRepository(),
                    testTTSClientRepository = repository
                )
            )
            context.requestUserUid = UserUid("42")
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
        val testResourceGet = ResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceId = ResourceId("test-id")
        val testResourceEntity = ResourceEntity(
            resourceId = testResourceId,
            data = ByteArray(42) { 42 }
        )

        val repository = MockTTSResourceRepository(
            invokeFindResourceId = { ResourceIdTTSResponse(testResourceId) },
            invokeGetResource = { ResourceEntityTTSResponse(testResourceEntity) },
        )

        val context = testContext(repository)
        context.requestResourceGet = testResourceGet

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResourceEntity, context.responseResourceEntity)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(1, repository.getResourceCounts.get())
    }

    @Test
    fun `test get resource fail no resource found`() = runTest {
        val testResourceGet = ResourceGet(word = "xxx", lang = LangId("en"))
        val testResourceId = ResourceId("test-id")
        val testResourceEntity = ResourceEntityTTSResponse(ResourceEntity(
            resourceId = testResourceId,
            data = ByteArray(42) { 42 }
        ))

        val repository = MockTTSResourceRepository(
            invokeFindResourceId = { ResourceIdTTSResponse.EMPTY },
            invokeGetResource = { testResourceEntity },
        )

        val context = testContext(repository)
        context.requestResourceGet = testResourceGet

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals("run::${CardOperation.GET_RESOURCE}", error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testResourceGet.toString(), error.field)
        Assertions.assertEquals("Error while GET_RESOURCE: no resource found. filter=${testResourceGet}", error.message)
        Assertions.assertNull(error.exception)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(0, repository.getResourceCounts.get())
    }

    @Test
    fun `test get resource fail exception`() = runTest {
        val testResourceGet = ResourceGet(word = "xxx", lang = LangId("EN"))
        val testResourceIdFound = ResourceIdTTSResponse(ResourceId("test-id"))

        val repository = MockTTSResourceRepository(
            invokeFindResourceId = { testResourceIdFound },
            invokeGetResource = { throw TestException() },
        )

        val context = testContext(repository)
        context.requestResourceGet = testResourceGet

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals(ResourceEntity.DUMMY, context.responseResourceEntity)

        val error = context.errors[0]
        Assertions.assertEquals("run::${CardOperation.GET_RESOURCE}", error.code)
        Assertions.assertEquals("run", error.group)
        Assertions.assertEquals(testResourceGet.toString(), error.field)
        Assertions.assertEquals("Error while GET_RESOURCE: unexpected exception", error.message)
        Assertions.assertInstanceOf(TestException::class.java, error.exception)

        Assertions.assertEquals(1, repository.findResourceIdCounts.get())
        Assertions.assertEquals(1, repository.getResourceCounts.get())
    }

    private class TestException : RuntimeException()
}