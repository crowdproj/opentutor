package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.DbRepositories
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.core.mappers.toDbDictionary
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbCardRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbDocumentRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbDocumentRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DictionaryCorProcessorRunTest {
    companion object {
        private val testUserId = testDictionaryEntity.userId

        @Suppress("SameParameterValue")
        private fun testContext(
            op: DictionaryOperation,
            dictionaryRepository: DbDictionaryRepository = MockDbDictionaryRepository(),
            documentRepository: DbDocumentRepository = MockDbDocumentRepository(),
            cardsRepository: DbCardRepository = MockDbCardRepository(),
            userRepository: MockDbUserRepository = MockDbUserRepository(),
        ): DictionaryContext {
            val context = DictionaryContext(
                operation = op,
                repositories = DbRepositories().copy(
                    dictionaryRepository = dictionaryRepository,
                    cardRepository = cardsRepository,
                    userRepository = userRepository,
                    documentRepository = documentRepository,
                )
            )
            context.requestAppAuthId = testUserId
            context.requestId = requestId(op)
            return context
        }

        private fun requestId(op: DictionaryOperation): AppRequestId {
            return AppRequestId("[for-${op}]")
        }
    }

    @Test
    fun `test get-all-dictionary success`() = runTest {
        val testResponseEntities = testDictionaryEntities

        var getAllDictionariesWasCalled = false
        var countCardsByDictionaryIdWasCalled = false
        var countCardsByDictionaryIdAndAnsweredWasCalled = false
        var getUserByIdWasCalled = false
        var createUserWasCalled = false
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeGetAllDictionaries = { userId ->
                getAllDictionariesWasCalled = true
                if (userId == testUserId.asString()) {
                    testResponseEntities.asSequence().map { it.toDbDictionary() }
                } else {
                    emptySequence()
                }
            }
        )
        val cardsRepository = MockDbCardRepository(
            invokeCountCardsByDictionaryIdIn = {
                countCardsByDictionaryIdWasCalled = true
                emptyMap()
            },
            invokeCountAnsweredCardsByDictionaryIdIn = { _, _ ->
                countCardsByDictionaryIdAndAnsweredWasCalled = true
                emptyMap()
            }
        )
        val userRepository = MockDbUserRepository(
            invokeFindUserById = {
                getUserByIdWasCalled = true
                null
            },
            invokeCreateUser = {
                createUserWasCalled = true
                it
            },
        )

        val context = testContext(
            op = DictionaryOperation.GET_ALL_DICTIONARIES,
            dictionaryRepository = dictionaryRepository,
            cardsRepository = cardsRepository,
            userRepository = userRepository,
        )

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { "has errors: ${context.errors}" }
        Assertions.assertTrue(getAllDictionariesWasCalled)
        Assertions.assertTrue(countCardsByDictionaryIdWasCalled)
        Assertions.assertTrue(countCardsByDictionaryIdAndAnsweredWasCalled)
        Assertions.assertTrue(getUserByIdWasCalled)
        Assertions.assertTrue(createUserWasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.GET_ALL_DICTIONARIES), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)

        Assertions.assertEquals(testResponseEntities, context.responseDictionaryEntityList)
    }

    @Test
    fun `test create-dictionary success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val givenDictionary =
            testDictionaryEntity.copy(dictionaryId = DictionaryId.NONE, name = " ${testDictionaryEntity.name} ")
        val expectedDictionary =
            testDictionaryEntity.copy(
                dictionaryId = testDictionaryId,
                sourceLang = testDictionaryEntity.sourceLang.normalize(),
                targetLang = testDictionaryEntity.targetLang.normalize(),
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
    fun `test update-dictionary success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val givenDictionary =
            testDictionaryEntity.copy(dictionaryId = testDictionaryId, name = " ${testDictionaryEntity.name} ")
        val expectedDictionary =
            testDictionaryEntity.copy(
                dictionaryId = testDictionaryId,
                sourceLang = testDictionaryEntity.sourceLang.normalize(),
                targetLang = testDictionaryEntity.targetLang.normalize(),
            )

        var wasCalled = false
        val repository = MockDbDictionaryRepository(
            invokeUpdateDictionary = { d ->
                wasCalled = true
                d.copy(dictionaryId = testDictionaryId.asString())
            }
        )

        val context = testContext(DictionaryOperation.UPDATE_DICTIONARY, repository)
        context.requestDictionaryEntity = givenDictionary

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { "Has errors: ${context.errors}" }
        Assertions.assertTrue(wasCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.UPDATE_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(expectedDictionary, context.responseDictionaryEntity)
    }

    @Test
    fun `test delete-dictionary success`() = runTest {
        val testId = DictionaryId("42")
        val response = testDictionaryEntity

        var isDeleteDictionaryCalled = false
        val repository = MockDbDictionaryRepository(
            invokeFindDictionaryById = {
                if (it == testId.asString()) response.toDbDictionary() else Assertions.fail()
            },
            invokeDeleteDictionary = {
                isDeleteDictionaryCalled = true
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
    fun `test download-dictionary xml success`() = runTest {
        val testId = DictionaryId("42")
        val testDictionary = testDictionaryEntity.copy(
            dictionaryId = testId,
            sourceLang = LangEntity(LangId("en")),
            targetLang = LangEntity(LangId("fr")),
        )
        val testCard = testCardEntity1.copy(dictionaryId = testId)

        var isFindDictionaryByIdCalled = false
        var isFindCardsByDictionaryIdCalled = false
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = {
                isFindDictionaryByIdCalled = true
                if (it == testId.asString()) testDictionary.toDbDictionary() else Assertions.fail()
            }
        )
        val cardsRepository = MockDbCardRepository(
            invokeFindCardsByDictionaryId = {
                isFindCardsByDictionaryIdCalled = true
                if (it == testId.asString()) sequenceOf(testCard.toDbCard()) else Assertions.fail()
            }
        )

        val context = testContext(
            op = DictionaryOperation.DOWNLOAD_DICTIONARY,
            dictionaryRepository = dictionaryRepository,
            cardsRepository = cardsRepository,
        )
        context.requestDictionaryId = testId
        context.requestDownloadDocumentType = "xml"

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(isFindDictionaryByIdCalled)
        Assertions.assertTrue(isFindCardsByDictionaryIdCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.DOWNLOAD_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        val document = context.responseDictionaryResourceEntity.data.toString(Charsets.UTF_16)
        Assertions.assertTrue(document.contains("<meaning partOfSpeech=\"1\" transcription=\"stʌb\">"))
        Assertions.assertTrue(document.contains("title=\"Stub-dictionary\""))
    }

    @Test
    fun `test download-dictionary json success`() = runTest {
        val testId = DictionaryId("42")
        val testDictionary = testDictionaryEntity.copy(
            dictionaryId = testId,
            sourceLang = LangEntity(LangId("en")),
            targetLang = LangEntity(LangId("fr")),
        )
        val testCard = testCardEntity1.copy(dictionaryId = testId)

        var isFindDictionaryByIdCalled = false
        var isFindCardsByDictionaryIdCalled = false
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = {
                isFindDictionaryByIdCalled = true
                if (it == testId.asString()) testDictionary.toDbDictionary() else Assertions.fail()
            }
        )
        val cardsRepository = MockDbCardRepository(
            invokeFindCardsByDictionaryId = {
                isFindCardsByDictionaryIdCalled = true
                if (it == testId.asString()) sequenceOf(testCard.toDbCard()) else Assertions.fail()
            }
        )

        val context = testContext(
            op = DictionaryOperation.DOWNLOAD_DICTIONARY,
            dictionaryRepository = dictionaryRepository,
            cardsRepository = cardsRepository,
        )
        context.requestDictionaryId = testId
        context.requestDownloadDocumentType = "json"

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(isFindDictionaryByIdCalled)
        Assertions.assertTrue(isFindCardsByDictionaryIdCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.DOWNLOAD_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        val document = context.responseDictionaryResourceEntity.data.toString(Charsets.UTF_8)
        println(document)
        Assertions.assertTrue(document.contains("\"name\": \"Stub-dictionary\""))
        Assertions.assertTrue(document.contains("\"cards\": ["))
    }

    @Test
    fun `test upload-dictionary xml success`() = runTest {
        val testDocument = ResourceEntity.DUMMY.copy(
            data = """
            <?xml version="1.0" encoding="UTF-16"?>
            <dictionary formatVersion="6" 
                title="test" 
                sourceLanguageId="1033" 
                destinationLanguageId="1049" 
                nextWordId="2" 
                targetNamespace="http://www.abbyy.com/TutorDictionary" 
                soundfile="testEnRu">
            	<statistics readyMeaningsQuantity="1" />
            	<card>
            		<word>test</word>
            		<meanings>
            			<meaning>
            				<statistics status="2" />
            				<translations>
            					<word>тест</word>
            				</translations>
            			</meaning>
            		</meanings>
            	</card>
            </dictionary>
        """.trimIndent().toByteArray(Charsets.UTF_16)
        )
        val testDictionary = testDictionaryEntity

        var isSaveDocumentCalled = false
        val documentRepository = MockDbDocumentRepository(
            invokeSave = { doc, cards ->
                isSaveDocumentCalled = true
                if (doc.name == "test") {
                    testDictionary.dictionaryId.asString()
                } else {
                    Assertions.fail()
                }
            }
        )

        val context = testContext(
            op = DictionaryOperation.UPLOAD_DICTIONARY,
            documentRepository = documentRepository,
        )
        context.requestDictionaryResourceEntity = testDocument
        context.requestDownloadDocumentType = "xml"

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { "errors: ${context.errors}" }
        Assertions.assertTrue(isSaveDocumentCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.UPLOAD_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
    }

    @Test
    fun `test upload-dictionary json success`() = runTest {
        val testDocument = ResourceEntity.DUMMY.copy(
            data = """
            {
                "name": "test",
                "sourceLang": {
                    "langId": "en"
                },
                "targetLang": {
                    "langId": "ru"
                },
                "cards": [
                    {
                        "words": [
                            {
                                "word": "test",
                                "transcription": "",
                                "partOfSpeech": "",
                                "translations": [
                                    [
                                        "тест"
                                    ]
                                ]
                            }
                        ],
                        "changedAt": "2024-07-21T10:13:25.523Z"
                    }
                ]
            }
        """.trimIndent().toByteArray(Charsets.UTF_8)
        )
        val testDictionary = testDictionaryEntity

        var isSaveDocumentCalled = false
        val documentRepository = MockDbDocumentRepository(
            invokeSave = { doc, cards ->
                isSaveDocumentCalled = true
                if (doc.name == "test") {
                    testDictionary.dictionaryId.asString()
                } else {
                    Assertions.fail()
                }
            }
        )

        val context = testContext(
            op = DictionaryOperation.UPLOAD_DICTIONARY,
            documentRepository = documentRepository,
        )
        context.requestDictionaryResourceEntity = testDocument
        context.requestDownloadDocumentType = "json"

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { "errors: ${context.errors}" }
        Assertions.assertTrue(isSaveDocumentCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.UPLOAD_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
    }
}