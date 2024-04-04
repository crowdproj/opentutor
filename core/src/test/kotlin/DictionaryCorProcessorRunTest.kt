package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.AppRepositories
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.mappers.toDbDictionary
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbCardRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.ImportDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import com.gitlab.sszuev.flashcards.stubs.stubDictionaries
import com.gitlab.sszuev.flashcards.stubs.stubDictionary
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DictionaryCorProcessorRunTest {
    companion object {
        private val testUser = AppUserEntity(AppUserId("42"), AppAuthId("00000000-0000-0000-0000-000000000000"))

        @Suppress("SameParameterValue")
        private fun testContext(
            op: DictionaryOperation,
            dictionaryRepository: DbDictionaryRepository,
            userRepository: DbUserRepository = MockDbUserRepository(
                invokeGetUser = { if (it == testUser.authId) UserEntityDbResponse(user = testUser) else throw AssertionError() }
            ),
            cardsRepository: DbCardRepository = MockDbCardRepository(),
        ): DictionaryContext {
            val context = DictionaryContext(
                operation = op,
                repositories = AppRepositories().copy(
                    testUserRepository = userRepository,
                    testDictionaryRepository = dictionaryRepository,
                    testCardRepository = cardsRepository,
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

        var getAllDictionariesWasCalled = false
        var getAllCardsWasCalled = false
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeGetAllDictionaries = { userId ->
                getAllDictionariesWasCalled = true
                if (userId == testUser.id.asString()) {
                    testResponseEntities.asSequence().map { it.toDbDictionary() }
                } else {
                    emptySequence()
                }
            }
        )
        val cardsRepository = MockDbCardRepository(
            invokeFindCardsByDictionaryId = { _ ->
                getAllCardsWasCalled = true
                emptySequence()
            }
        )

        val context = testContext(
            op = DictionaryOperation.GET_ALL_DICTIONARIES,
            dictionaryRepository = dictionaryRepository,
            cardsRepository = cardsRepository
        )

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(getAllDictionariesWasCalled)
        Assertions.assertTrue(getAllCardsWasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.GET_ALL_DICTIONARIES), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntities, context.responseDictionaryEntityList)
    }

    @Test
    fun `test create-dictionary success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val givenDictionary =
            stubDictionary.copy(dictionaryId = DictionaryId.NONE, name = " ${stubDictionary.name} ")
        val expectedDictionary =
            stubDictionary.copy(
                dictionaryId = testDictionaryId,
                sourceLang = stubDictionary.sourceLang.normalize(),
                targetLang = stubDictionary.targetLang.normalize(),
            )

        var wasCalled = false
        val repository = MockDbDictionaryRepository(
            invokeCreateDictionary = { d ->
                wasCalled = true
                d.copy(dictionaryId = testDictionaryId.asString())
            }
        )

        val context = testContext(DictionaryOperation.CREATE_DICTIONARY, repository)
        context.requestDictionaryEntity = givenDictionary

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { "Has errors: ${context.errors}" }
        Assertions.assertTrue(wasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.CREATE_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(expectedDictionary, context.responseDictionaryEntity)
    }

    @Test
    fun `test delete-dictionary success`() = runTest {
        val testId = DictionaryId("42")
        val response = stubDictionary

        var isDeleteDictionaryCalled = false
        val repository = MockDbDictionaryRepository(
            invokeDeleteDictionary = {
                isDeleteDictionaryCalled = true
                if (it == testId.asString()) response.toDbDictionary() else Assertions.fail()
            },
            invokeFindDictionaryById = {
                if (it == testId.asString()) response.toDbDictionary() else Assertions.fail()
            }
        )

        val context = testContext(DictionaryOperation.DELETE_DICTIONARY, repository)
        context.requestDictionaryId = testId

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(isDeleteDictionaryCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.DELETE_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
    }

    @Test
    fun `test download-dictionary success`() = runTest {
        val testId = DictionaryId("42")
        val testData = ResourceEntity(testId, ByteArray(42) { 42 })
        val response = ImportDictionaryDbResponse(resource = testData)

        var wasCalled = false
        val repository = MockDbDictionaryRepository(
            invokeDownloadDictionary = { _, it ->
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