package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.*
import com.gitlab.sszuev.flashcards.stubs.stubDictionaries
import com.gitlab.sszuev.flashcards.stubs.stubDictionary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DictionaryCorProcessorRunTest {
    companion object {
        private val testUser = AppUserEntity(AppUserId("42"), AppAuthId("00000000-0000-0000-0000-000000000000"))

        @Suppress("SameParameterValue")
        private fun testContext(
            op: DictionaryOperation,
            dictionaryRepository: DbDictionaryRepository,
            userRepository: DbUserRepository = MockDbUserRepository(
                invokeGetUser = { if (it == testUser.authId) UserEntityDbResponse(user = testUser) else throw AssertionError() }
            )
        ): DictionaryContext {
            val context = DictionaryContext(
                operation = op,
                repositories = DictionaryRepositories().copy(
                    testUserRepository = userRepository,
                    testDictionaryRepository = dictionaryRepository
                )
            )
            context.requestAppAuthId = testUser.authId
            context.workMode = AppMode.TEST
            context.requestId = requestId(op)
            return context
        }

        private fun requestId(op: DictionaryOperation): AppRequestId {
            return AppRequestId("[for-${op}]")
        }
    }

    @Test
    fun `test get-all-dictionary success`() = runTest {
        val testResponseEntities = stubDictionaries

        var wasCalled = false
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeGetAllDictionaries = {
                wasCalled = true
                DictionariesDbResponse(if (it == testUser.id) testResponseEntities else emptyList())
            }
        )

        val context = testContext(DictionaryOperation.GET_ALL_DICTIONARIES, dictionaryRepository)

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(wasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.GET_ALL_DICTIONARIES), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntities, context.responseDictionaryEntityList)
    }

    @Test
    fun `test delete-dictionary success`() = runTest {
        val testId = DictionaryId("42")
        val response = DeleteDictionaryDbResponse()

        var wasCalled = false
        val repository = MockDbDictionaryRepository(
            invokeDeleteDictionary = {
                wasCalled = true
                if (it == testId) response else throw AssertionError()
            }
        )

        val context = testContext(DictionaryOperation.DELETE_DICTIONARY, repository)
        context.requestDictionaryId = testId

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(wasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.DELETE_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
    }

    @Test
    fun `test download-dictionary success`() = runTest {
        val testId = DictionaryId("42")
        val testData = ResourceEntity(testId, ByteArray(42) { 42 })
        val response = DownloadDictionaryDbResponse(testData)

        var wasCalled = false
        val repository = MockDbDictionaryRepository(
            invokeDownloadDictionary = {
                wasCalled = true
                if (it == testId) response else throw AssertionError()
            }
        )

        val context = testContext(DictionaryOperation.DOWNLOAD_DICTIONARY, repository)
        context.requestDictionaryId = testId

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(wasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.DOWNLOAD_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
    }

    @Test
    fun `test upload-dictionary success`() = runTest {
        val testData = ResourceEntity(DictionaryId.NONE, ByteArray(4200) { 42 })
        val response = DictionaryDbResponse(dictionary = stubDictionary)

        var wasCalled = false
        val repository = MockDbDictionaryRepository(
            invokeUploadDictionary = { id, bytes ->
                wasCalled = true
                if (id != testUser.id) throw AssertionError()
                if (bytes != testData) throw AssertionError()
                response
            }
        )

        val context = testContext(DictionaryOperation.UPLOAD_DICTIONARY, repository)
        context.requestDictionaryResourceEntity = testData

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { "errors: ${context.errors}" }
        Assertions.assertTrue(wasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.UPLOAD_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
    }
}