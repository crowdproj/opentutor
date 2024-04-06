package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.AppRepositories
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.core.mappers.toDbDictionary
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbCardRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbDictionaryRepository
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubDictionaries
import com.gitlab.sszuev.flashcards.stubs.stubDictionary
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DictionaryCorProcessorRunTest {
    companion object {
        private val testUserId = stubDictionary.userId

        @Suppress("SameParameterValue")
        private fun testContext(
            op: DictionaryOperation,
            dictionaryRepository: DbDictionaryRepository,
            cardsRepository: DbCardRepository = MockDbCardRepository(),
        ): DictionaryContext {
            val context = DictionaryContext(
                operation = op,
                repositories = AppRepositories().copy(
                    testDictionaryRepository = dictionaryRepository,
                    testCardRepository = cardsRepository,
                )
            )
            context.requestAppAuthId = testUserId
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
                if (userId == testUserId.asString()) {
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
    fun `test download-dictionary success`() = runTest {
        val testId = DictionaryId("42")
        val testDictionary = stubDictionary.copy(
            dictionaryId = testId,
            sourceLang = LangEntity(LangId("en")),
            targetLang = LangEntity(LangId("fr")),
        )
        val testCard = stubCard.copy(dictionaryId = testId)

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
    fun `test upload-dictionary success`() = runTest {
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
        val testDictionary = stubDictionary
        val testCard = stubCard

        var isCreateDictionaryCalled = false
        var isCreateCardsCalled = false
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeCreateDictionary = {
                isCreateDictionaryCalled = true
                if (it.name == "test") {
                    testDictionary.toDbDictionary()
                } else {
                    Assertions.fail()
                }
            }
        )
        val cardsRepository = MockDbCardRepository(
            invokeCreateCards = {
                isCreateCardsCalled = true
                val cards = it.toList()
                if (cards.size == 1 &&
                    cards[0].words.single().word == "test" &&
                    cards[0].dictionaryId == testDictionary.dictionaryId.asString()
                ) {
                    listOf(testCard.toDbCard())
                } else {
                    Assertions.fail()
                }
            }
        )

        val context = testContext(
            op = DictionaryOperation.UPLOAD_DICTIONARY,
            dictionaryRepository = dictionaryRepository,
            cardsRepository = cardsRepository,
        )
        context.requestDictionaryResourceEntity = testDocument

        DictionaryCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { "errors: ${context.errors}" }
        Assertions.assertTrue(isCreateDictionaryCalled)
        Assertions.assertTrue(isCreateCardsCalled)
        Assertions.assertEquals(requestId(DictionaryOperation.UPLOAD_DICTIONARY), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
    }
}