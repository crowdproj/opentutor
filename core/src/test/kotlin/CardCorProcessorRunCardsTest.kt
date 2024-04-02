package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.AppRepositories
import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.core.mappers.toDbDictionary
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbCardRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbDictionaryRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppUserEntity
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceIdResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.repositories.UserEntityDbResponse
import com.gitlab.sszuev.flashcards.speaker.MockTTSResourceRepository
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards
import com.gitlab.sszuev.flashcards.stubs.stubDictionaries
import com.gitlab.sszuev.flashcards.stubs.stubDictionary
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class CardCorProcessorRunCardsTest {
    companion object {
        private val testUser = AppUserEntity(AppUserId("42"), AppAuthId("00000000-0000-0000-0000-000000000000"))

        private fun testContext(
            op: CardOperation,
            cardRepository: DbCardRepository,
            userRepository: DbUserRepository = MockDbUserRepository(),
            dictionaryRepository: DbDictionaryRepository = MockDbDictionaryRepository(),
            ttsResourceRepository: TTSResourceRepository = MockTTSResourceRepository(invokeFindResourceId = {
                TTSResourceIdResponse.EMPTY.copy(TTSResourceId(it.lang.asString() + ":" + it.word))
            }),
        ): CardContext {
            val context = CardContext(
                operation = op,
                repositories = AppRepositories().copy(
                    testUserRepository = userRepository,
                    testCardRepository = cardRepository,
                    testDictionaryRepository = dictionaryRepository,
                    testTTSClientRepository = ttsResourceRepository,
                )
            )
            context.requestAppAuthId = testUser.authId
            context.workMode = AppMode.TEST
            context.requestId = requestId(op)
            return context
        }

        private fun requestId(op: CardOperation): AppRequestId {
            return AppRequestId("[for-${op}]")
        }

        private fun assertUnknownError(context: CardContext, op: CardOperation) {
            val error = assertSingleError(context, op)
            Assertions.assertEquals("run::$op", error.code)
            Assertions.assertEquals("run", error.group)
            Assertions.assertEquals("Error while $op: exception", error.message)
            Assertions.assertInstanceOf(TestException::class.java, error.exception)
        }

        private fun assertSingleError(context: CardContext, op: CardOperation): AppError {
            Assertions.assertEquals(requestId(op), context.requestId)
            Assertions.assertEquals(AppStatus.FAIL, context.status)
            Assertions.assertEquals(1, context.errors.size)
            return context.errors[0]
        }
    }

    @Test
    fun `test get-card success`() = runTest {
        val testId = CardId("42")
        val testResponseCardEntity = stubCard.copy(cardId = testId)
        val testResponseDictionaryEntity = stubDictionary

        var findCardIsCalled = false
        var findDictionaryIsCalled = false
        val cardRepository = MockDbCardRepository(
            invokeFindCardById = { cardId ->
                findCardIsCalled = true
                if (cardId == testId.asString()) testResponseCardEntity.toDbCard() else null
            }
        )

        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = { dictionaryId ->
                findDictionaryIsCalled = true
                if (dictionaryId == testResponseDictionaryEntity.dictionaryId.asString()) {
                    testResponseDictionaryEntity.toDbDictionary()
                } else {
                    null
                }
            }
        )

        val context = testContext(
            op = CardOperation.GET_CARD,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardEntityId = testId

        CardCorProcessor().execute(context)

        Assertions.assertTrue(findCardIsCalled)
        Assertions.assertTrue(findDictionaryIsCalled)
        Assertions.assertEquals(requestId(CardOperation.GET_CARD), context.requestId)
        Assertions.assertTrue(context.errors.isEmpty()) { context.errors.toString() }
        Assertions.assertEquals(AppStatus.OK, context.status)

        Assertions.assertEquals(testResponseCardEntity, context.responseCardEntity)
    }

    @Test
    fun `test get-card error - unexpected fail`() = runTest {
        val testCardId = CardId("42")

        var getUserWasCalled = false
        var getIsWasCalled = false
        val cardRepository = MockDbCardRepository(
            invokeFindCardById = { _ ->
                getIsWasCalled = true
                throw TestException()
            }
        )
        val userRepository = MockDbUserRepository(
            invokeGetUser = {
                getUserWasCalled = true
                if (it == testUser.authId) UserEntityDbResponse(user = testUser) else throw TestException()
            }
        )

        val context =
            testContext(op = CardOperation.GET_CARD, cardRepository = cardRepository, userRepository = userRepository)
        context.requestAppAuthId = testUser.authId
        context.requestCardEntityId = testCardId

        CardCorProcessor().execute(context)

        Assertions.assertTrue(getUserWasCalled)
        Assertions.assertTrue(getIsWasCalled)
        Assertions.assertEquals(requestId(CardOperation.GET_CARD), context.requestId)
        assertUnknownError(context, CardOperation.GET_CARD)
    }

    @Test
    fun `test get-all-cards success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testDictionary = stubDictionary.copy(dictionaryId = testDictionaryId)
        val testCards = stubCards.map { it.copy(dictionaryId = testDictionaryId) }

        var isFindCardsCalled = false
        var isFindDictionaryCalled = false
        val cardRepository = MockDbCardRepository(
            invokeFindCardsByDictionaryId = { id ->
                isFindCardsCalled = true
                if (id == testDictionaryId.asString()) testCards.map { it.toDbCard() }.asSequence() else emptySequence()
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = { id ->
                isFindDictionaryCalled = true
                if (id == testDictionaryId.asString()) testDictionary.toDbDictionary() else null
            }
        )

        val context = testContext(
            op = CardOperation.GET_ALL_CARDS,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor().execute(context)

        Assertions.assertTrue(context.errors.isEmpty()) { context.errors.toString() }
        Assertions.assertTrue(isFindCardsCalled)
        Assertions.assertTrue(isFindDictionaryCalled)
        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)

        Assertions.assertEquals(
            testCards.size,
            (context.repositories.ttsClientRepository(AppMode.TEST) as MockTTSResourceRepository).findResourceIdCounts.toInt()
        )

        Assertions.assertEquals(testCards, context.responseCardEntityList)
    }

    @Test
    fun `test get-all-cards unexpected fail`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testDictionary = stubDictionary.copy(dictionaryId = testDictionaryId)
        val testResponseEntities = stubCards

        var isFindCardsCalled = false
        var isFindDictionaryCalled = false
        val repository = MockDbCardRepository(
            invokeFindCardsByDictionaryId = { id ->
                isFindCardsCalled = true
                if (id != testDictionaryId.asString()) {
                    testResponseEntities.map { it.toDbCard() }.asSequence()
                } else throw TestException()
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = { id ->
                isFindDictionaryCalled = true
                if (id == testDictionaryId.asString()) testDictionary.toDbDictionary() else null
            }
        )

        val context = testContext(
            op = CardOperation.GET_ALL_CARDS,
            cardRepository = repository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isFindDictionaryCalled)
        Assertions.assertTrue(isFindCardsCalled)
        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(0, context.responseCardEntityList.size)
        assertUnknownError(context, CardOperation.GET_ALL_CARDS)
    }

    @Test
    fun `test create-card success`() = runTest {
        val testDictionary = stubDictionary
        val testResponseEntity = stubCard.copy(
            words = listOf(CardWordEntity(word = "HHH", sound = TTSResourceId("sl:HHH"))),
            sound = TTSResourceId("sl:HHH")
        )
        val testRequestEntity = stubCard.copy(
            words = listOf(CardWordEntity(word = "XXX")),
            cardId = CardId.NONE,
            dictionaryId = DictionaryId("4200")
        )

        var isCreateCardCalled = false
        var isFindDictionaryCalled = false
        val cardRepository = MockDbCardRepository(
            invokeCreateCard = { card ->
                isCreateCardCalled = true
                if (card.words.map { it.word } == testRequestEntity.words.map { it.word }) {
                    testResponseEntity.toDbCard()
                } else {
                    testRequestEntity.toDbCard()
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = { id ->
                isFindDictionaryCalled = true
                if (id == testRequestEntity.dictionaryId.asString()) testDictionary.toDbDictionary() else null
            }
        )

        val context = testContext(
            op = CardOperation.CREATE_CARD,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isCreateCardCalled)
        Assertions.assertTrue(isFindDictionaryCalled)
        Assertions.assertEquals(requestId(CardOperation.CREATE_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status) { "Errors: ${context.errors}" }
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test create-card unexpected fail`() = runTest {
        val testDictionary = stubDictionary
        val testRequestEntity = stubCard.copy(words = listOf(CardWordEntity(word = "XXX")), cardId = CardId.NONE)

        var isCreateCardCalled = false
        var isFindDictionaryCalled = false
        val cardRepository = MockDbCardRepository(
            invokeCreateCard = { card ->
                isCreateCardCalled = true
                if (card.words.map { it.word } == testRequestEntity.words.map { it.word }) {
                    throw TestException()
                } else {
                    testRequestEntity.toDbCard()
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = { id ->
                isFindDictionaryCalled = true
                if (id == testRequestEntity.dictionaryId.asString()) testDictionary.toDbDictionary() else null
            }
        )

        val context = testContext(
            op = CardOperation.CREATE_CARD,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isCreateCardCalled)
        Assertions.assertTrue(isFindDictionaryCalled)
        Assertions.assertEquals(requestId(CardOperation.CREATE_CARD), context.requestId)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
        assertUnknownError(context, CardOperation.CREATE_CARD)
    }

    @Test
    fun `test search-cards success`() = runTest {
        val testFilter = CardFilter(
            dictionaryIds = listOf(DictionaryId("42")),
            random = false,
            onlyUnknown = true,
            length = 42,
        )
        val testCards = stubCards.filter { it.dictionaryId in testFilter.dictionaryIds }
        val testDictionaries = stubDictionaries

        var isFindCardsCalled = false
        var isFindDictionariesCalled = false
        val cardRepository = MockDbCardRepository(
            invokeFindCardsByDictionaryIdIn = { ids ->
                isFindCardsCalled = true
                if (ids == testFilter.dictionaryIds.map { it.asString() }) {
                    testCards.asSequence().map { it.toDbCard() }
                } else {
                    emptySequence()
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionariesByIdIn = { givenDictionaryIds ->
                isFindDictionariesCalled = true
                if (givenDictionaryIds == testFilter.dictionaryIds.map { it.asString() }) {
                    testDictionaries.asSequence().map { it.toDbDictionary() }
                } else {
                    emptySequence()
                }
            }
        )

        val context = testContext(
            op = CardOperation.SEARCH_CARDS,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardFilter = testFilter

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isFindCardsCalled)
        Assertions.assertTrue(isFindDictionariesCalled)
        Assertions.assertEquals(requestId(CardOperation.SEARCH_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testCards, context.responseCardEntityList)
    }

    @Test
    fun `test search-cards unexpected fail`() = runTest {
        val testFilter = CardFilter(
            dictionaryIds = listOf(DictionaryId("42")),
            random = false,
            onlyUnknown = false,
            length = 1,
        )
        val testDictionaries = stubDictionaries
        val testCards = stubCards

        var isFindCardsCalled = false
        var isFindDictionariesCalled = false
        val cardRepository = MockDbCardRepository(
            invokeFindCardsByDictionaryIdIn = { ids ->
                isFindCardsCalled = true
                if (ids == testFilter.dictionaryIds.map { it.asString() }) {
                    throw TestException()
                } else {
                    testCards.asSequence().map { it.toDbCard() }
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionariesByIdIn = { givenDictionaryIds ->
                isFindDictionariesCalled = true
                if (givenDictionaryIds == testFilter.dictionaryIds.map { it.asString() }) {
                    testDictionaries.asSequence().map { it.toDbDictionary() }
                } else {
                    emptySequence()
                }
            }
        )

        val context = testContext(
            op = CardOperation.SEARCH_CARDS,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardFilter = testFilter

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isFindDictionariesCalled)
        Assertions.assertTrue(isFindCardsCalled)
        Assertions.assertEquals(0, context.responseCardEntityList.size)
        assertUnknownError(context, CardOperation.SEARCH_CARDS)
    }

    @Test
    fun `test update-card success`() = runTest {
        val cardId = CardId("42")
        val testDictionary = stubDictionary
        val testRequestEntity = stubCard.copy(words = listOf(CardWordEntity(word = "XXX")), cardId = cardId)
        val testResponseEntity = stubCard

        var isUpdateCardCalled = false
        var isFindDictionaryCalled = false
        val cardRepository = MockDbCardRepository(
            invokeUpdateCard = {
                isUpdateCardCalled = true
                if (it.cardId == cardId.asString()) {
                    testResponseEntity.toDbCard()
                } else {
                    testRequestEntity.toDbCard()
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = {
                isFindDictionaryCalled = true
                if (testDictionary.dictionaryId.asString() == it) {
                    testDictionary.toDbDictionary()
                } else {
                    Assertions.fail()
                }
            }
        )

        val context = testContext(
            op = CardOperation.UPDATE_CARD,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isUpdateCardCalled)
        Assertions.assertTrue(isFindDictionaryCalled)
        Assertions.assertEquals(requestId(CardOperation.UPDATE_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status) { "Errors: ${context.errors}" }
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test update-card unexpected fail`() = runTest {
        val cardId = CardId("42")
        val testDictionary = stubDictionary
        val testRequestEntity = stubCard.copy(words = listOf(CardWordEntity(word = "XXX")), cardId = cardId)

        var isUpdateCardCalled = false
        var isFindDictionaryCalled = false
        val cardRepository = MockDbCardRepository(
            invokeUpdateCard = {
                isUpdateCardCalled = true
                if (it.cardId == testRequestEntity.cardId.asString()) {
                    throw TestException()
                } else {
                    testRequestEntity.toDbCard()
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = {
                isFindDictionaryCalled = true
                if (testDictionary.dictionaryId.asString() == it) {
                    testDictionary.toDbDictionary()
                } else {
                    Assertions.fail()
                }
            }
        )

        val context = testContext(
            op = CardOperation.UPDATE_CARD,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )

        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isUpdateCardCalled)
        Assertions.assertTrue(isFindDictionaryCalled)
        Assertions.assertEquals(requestId(CardOperation.UPDATE_CARD), context.requestId)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
        assertUnknownError(context, CardOperation.UPDATE_CARD)
    }

    @Test
    fun `test learn-cards success`() = runTest {
        val testLearn = listOf(
            CardLearn(cardId = stubCard.cardId, details = mapOf(Stage.WRITING to 42)),
        )

        val testCards = listOf(stubCard)
        val testDictionaries = listOf(stubDictionary)
        val expectedCards = listOf(
            stubCard.copy(
                answered = 42,
                stats = stubCard.stats + mapOf(Stage.WRITING to 42)
            )
        )

        var isUpdateCardsCalled = false
        var isFindDictionariesCalled = false
        val cardRepository = MockDbCardRepository(
            invokeUpdateCards = { givenCards ->
                isUpdateCardsCalled = true
                if (givenCards.map { it.cardId } == expectedCards.map { it.cardId.asString() }) {
                    expectedCards.map { it.toDbCard() }
                } else {
                    emptyList()
                }
            },
            invokeFindCardsByIdIn = { ids ->
                if (ids == listOf(stubCard.cardId.asString())) {
                    testCards.asSequence().map { it.toDbCard() }
                } else {
                    emptySequence()
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionariesByIdIn = { givenDictionaryIds ->
                isFindDictionariesCalled = true
                if (testDictionaries.map { it.dictionaryId.asString() } == givenDictionaryIds) {
                    testDictionaries.asSequence().map { it.toDbDictionary() }
                } else {
                    Assertions.fail()
                }
            }
        )

        val context = testContext(
            op = CardOperation.LEARN_CARDS,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardLearnList = testLearn

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isUpdateCardsCalled)
        Assertions.assertTrue(isFindDictionariesCalled)
        Assertions.assertEquals(requestId(CardOperation.LEARN_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(expectedCards, context.responseCardEntityList)
    }

    @Test
    fun `test learn-cards error`() = runTest {
        val testLearn = listOf(
            CardLearn(cardId = CardId("1"), details = mapOf(Stage.SELF_TEST to 42)),
            CardLearn(cardId = CardId("2"), details = mapOf(Stage.OPTIONS to 2, Stage.MOSAIC to 3))
        )
        val ids = testLearn.map { it.cardId }.map { it.asString() }

        val testCards = listOf(stubCard.copy(cardId = CardId("1")), stubCard.copy(cardId = CardId("2")))
        val testDictionaries = listOf(stubDictionary)

        var isUpdateCardsCalled = false
        var isFindDictionariesCalled = false
        val cardRepository = MockDbCardRepository(
            invokeUpdateCards = { givenCards ->
                isUpdateCardsCalled = true
                if (givenCards.map { it.cardId } == ids) {
                    throw TestException()
                } else {
                    emptyList()
                }
            },
            invokeFindCardsByIdIn = { givenIds ->
                if (givenIds == ids) {
                    testCards.asSequence().map { it.toDbCard() }
                } else {
                    emptySequence()
                }
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionariesByIdIn = { givenDictionaryIds ->
                isFindDictionariesCalled = true
                if (testDictionaries.map { it.dictionaryId.asString() } == givenDictionaryIds) {
                    testDictionaries.asSequence().map { it.toDbDictionary() }
                } else {
                    Assertions.fail()
                }
            }
        )

        val context = testContext(
            op = CardOperation.LEARN_CARDS,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardLearnList = testLearn

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isUpdateCardsCalled)
        Assertions.assertTrue(isFindDictionariesCalled)
        Assertions.assertEquals(requestId(CardOperation.LEARN_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(emptyList<CardEntity>(), context.responseCardEntityList)

        Assertions.assertEquals(1, context.errors.size)
        Assertions.assertEquals("run::LEARN_CARDS", context.errors[0].code)
        Assertions.assertInstanceOf(TestException::class.java, context.errors[0].exception)
    }

    @Test
    fun `test reset-card success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testCardId = CardId("42")
        val testDictionary = stubDictionary.copy(dictionaryId = testDictionaryId)
        val testCard = stubCard.copy(cardId = testCardId, answered = 42, dictionaryId = testDictionaryId)
        val expectedCard = testCard.copy(answered = 0)

        var isUpdateCardCalled = false
        val cardRepository = MockDbCardRepository(
            invokeUpdateCard = {
                isUpdateCardCalled = true
                if (it.cardId == testCardId.asString()) it else Assertions.fail()
            },
            invokeFindCardById = {
                if (it == testCardId.asString()) testCard.toDbCard() else null
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = {
                if (it == testDictionaryId.asString()) testDictionary.toDbDictionary() else null
            }
        )

        val context = testContext(
            op = CardOperation.RESET_CARD,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardEntityId = testCardId

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isUpdateCardCalled)
        Assertions.assertEquals(requestId(CardOperation.RESET_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(expectedCard, context.responseCardEntity)
    }

    @Test
    fun `test delete-card success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testCardId = CardId("42")
        val testCard = stubCard.copy(cardId = testCardId, dictionaryId = testDictionaryId)
        val testDictionary = stubDictionary.copy(dictionaryId = testDictionaryId)

        var isDeleteCardCalled = false
        val cardRepository = MockDbCardRepository(
            invokeDeleteCard = {
                isDeleteCardCalled = true
                if (it == testCardId.asString()) testCard.toDbCard() else Assertions.fail()
            },
            invokeFindCardById = {
                if (it == testCardId.asString()) testCard.toDbCard() else Assertions.fail()
            }
        )
        val dictionaryRepository = MockDbDictionaryRepository(
            invokeFindDictionaryById = {
                if (it == testDictionaryId.asString()) testDictionary.toDbDictionary() else Assertions.fail()
            }
        )

        val context = testContext(
            op = CardOperation.DELETE_CARD,
            cardRepository = cardRepository,
            dictionaryRepository = dictionaryRepository,
        )
        context.requestCardEntityId = testCardId

        CardCorProcessor().execute(context)

        Assertions.assertTrue(isDeleteCardCalled)
        Assertions.assertEquals(requestId(CardOperation.DELETE_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
    }

    @ParameterizedTest
    @EnumSource(value = CardOperation::class, names = ["NONE", "GET_RESOURCE"], mode = EnumSource.Mode.EXCLUDE)
    fun `test no user found`(op: CardOperation) = runTest {
        val testUid = AppAuthId("21")
        val testError = AppError(group = "test-error", code = "test-error")

        val testCardId = CardId("42")
        val testDictionaryId = DictionaryId("42")
        val testLearn = CardLearn(testCardId, mapOf(Stage.SELF_TEST to 42))
        val testCardEntity = CardEntity(
            cardId = if (op == CardOperation.UPDATE_CARD) testCardId else CardId.NONE,
            dictionaryId = testDictionaryId,
            words = listOf(
                CardWordEntity(
                    word = "xxx",
                    translations = listOf(listOf("fff")),
                )
            )
        )
        val testSearchFilter = CardFilter(
            dictionaryIds = listOf(testDictionaryId),
            length = 42,
        )

        var getUserIsCalled = false
        var getCardIsCalled = false
        val cardRepository = MockDbCardRepository(invokeFindCardById = { _ ->
            getCardIsCalled = true
            throw TestException()
        })
        val userRepository = MockDbUserRepository(invokeGetUser = {
            getUserIsCalled = true
            UserEntityDbResponse(user = AppUserEntity.EMPTY, errors = listOf(testError))
        })

        val context = testContext(op, cardRepository = cardRepository, userRepository = userRepository)
        context.requestAppAuthId = testUid
        context.requestCardEntityId = testCardId
        context.requestCardLearnList = listOf(testLearn)
        context.requestCardEntity = testCardEntity
        context.requestCardFilter = testSearchFilter
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor().execute(context)

        Assertions.assertTrue(getUserIsCalled)
        Assertions.assertFalse(getCardIsCalled)
        Assertions.assertEquals(requestId(op), context.requestId)
        val actual = assertSingleError(context, op)
        Assertions.assertSame(testError, actual)
    }
}