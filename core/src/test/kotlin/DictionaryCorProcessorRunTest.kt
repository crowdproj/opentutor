package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.repositories.*
import com.gitlab.sszuev.flashcards.stubs.stubDictionaries
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
            userRepository: DbUserRepository = MockDbUserRepository()
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

        val userRepository = MockDbUserRepository(
            invokeGetUser = { if (it == testUser.authId) UserEntityDbResponse(user = testUser) else throw TestException() }
        )

        var wasCalled = false
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeGetAllDictionaries = {
                wasCalled = true
                DictionariesDbResponse(if (it == testUser.id) testResponseEntities else emptyList())
            }
        )

        val context = testContext(DictionaryOperation.GET_ALL_DICTIONARIES, dictionaryRepository, userRepository)

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
                if (it == testId) response else throw TestException()
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
}