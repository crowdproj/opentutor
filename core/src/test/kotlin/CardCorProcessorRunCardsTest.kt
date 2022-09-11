package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbCardRepository
import com.gitlab.sszuev.flashcards.dbcommon.mocks.MockDbUserRepository
import com.gitlab.sszuev.flashcards.model.common.*
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.*
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@Suppress("OPT_IN_USAGE")
internal class CardCorProcessorRunCardsTest {
    companion object {
        private val testUser = AppUserEntity(AppUserId("42"), AppAuthId("00000000-0000-0000-0000-000000000000"))

        private fun testContext(
            op: CardOperation,
            cardRepository: DbCardRepository,
            userRepository: DbUserRepository = MockDbUserRepository()
        ): CardContext {
            val context = CardContext(
                operation = op,
                repositories = CardRepositories().copy(
                    testUserRepository = userRepository,
                    testCardRepository = cardRepository
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
        val testResponseEntity = stubCard.copy(cardId = testId)

        val repository = MockDbCardRepository(
            invokeGetCard = {
                CardEntityDbResponse(if (it == testId) testResponseEntity else CardEntity.EMPTY)
            }
        )

        val context = testContext(CardOperation.GET_CARD, repository)
        context.requestCardEntityId = testId

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test get-card error - unexpected fail`() = runTest {
        val testCardId = CardId("42")

        val cardRepository = MockDbCardRepository(invokeGetCard = { throw TestException() })
        val userRepository = MockDbUserRepository(
            invokeGetUser = { if (it == testUser.authId) UserEntityDbResponse(user = testUser) else throw TestException() }
        )

        val context =
            testContext(CardOperation.GET_CARD, cardRepository = cardRepository, userRepository = userRepository)
        context.requestAppAuthId = testUser.authId
        context.requestCardEntityId = testCardId

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_CARD), context.requestId)
        assertUnknownError(context, CardOperation.GET_CARD)
    }

    @Test
    fun `test get-all-cards success`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeGetAllCards = {
                CardEntitiesDbResponse(if (it == testDictionaryId) testResponseEntities else emptyList())
            }
        )

        val context = testContext(CardOperation.GET_ALL_CARDS, repository)
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test get-all-cards unexpected fail`() = runTest {
        val testDictionaryId = DictionaryId("42")
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeGetAllCards = {
                CardEntitiesDbResponse(
                    if (it != testDictionaryId)
                        testResponseEntities
                    else throw TestException()
                )
            }
        )

        val context = testContext(CardOperation.GET_ALL_CARDS, repository)
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.GET_ALL_CARDS), context.requestId)
        Assertions.assertEquals(0, context.responseCardEntityList.size)
        assertUnknownError(context, CardOperation.GET_ALL_CARDS)
    }

    @Test
    fun `test create-card success`() = runTest {
        val testResponseEntity = stubCard.copy(word = "HHH")
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = CardId.NONE)

        val repository = MockDbCardRepository(
            invokeCreateCard = {
                CardEntityDbResponse(if (it.word == testRequestEntity.word) testResponseEntity else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.CREATE_CARD, repository)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.CREATE_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status) { "Errors: ${context.errors}" }
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test create-card unexpected fail`() = runTest {
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = CardId.NONE)

        val repository = MockDbCardRepository(
            invokeCreateCard = {
                CardEntityDbResponse(if (it.word == testRequestEntity.word) throw TestException() else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.CREATE_CARD, repository)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.CREATE_CARD), context.requestId)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
        assertUnknownError(context, CardOperation.CREATE_CARD)
    }

    @Test
    fun `test search-cards success`() = runTest {
        val testFilter = CardFilter(
            dictionaryIds = listOf(DictionaryId("21"), DictionaryId("42")),
            random = true,
            withUnknown = true,
            length = 42,
        )
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeSearchCards = {
                CardEntitiesDbResponse(if (it == testFilter) testResponseEntities else emptyList())
            }
        )

        val context = testContext(CardOperation.SEARCH_CARDS, repository)
        context.requestCardFilter = testFilter

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.SEARCH_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())

        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test search-cards unexpected fail`() = runTest {
        val testFilter = CardFilter(
            dictionaryIds = listOf(DictionaryId("42")),
            random = false,
            withUnknown = false,
            length = 1,
        )
        val testResponseEntities = stubCards

        val repository = MockDbCardRepository(
            invokeSearchCards = {
                CardEntitiesDbResponse(
                    if (it != testFilter)
                        testResponseEntities
                    else throw TestException()
                )
            }
        )

        val context = testContext(CardOperation.SEARCH_CARDS, repository)
        context.requestCardFilter = testFilter

        CardCorProcessor().execute(context)
        Assertions.assertEquals(0, context.responseCardEntityList.size)
        assertUnknownError(context, CardOperation.SEARCH_CARDS)
    }

    @Test
    fun `test update-card success`() = runTest {
        val cardId = CardId("42")
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = cardId)
        val testResponseEntity = stubCard.copy(word = "HHH")

        val repository = MockDbCardRepository(
            invokeUpdateCard = {
                CardEntityDbResponse(if (it.cardId == cardId) testResponseEntity else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.UPDATE_CARD, repository)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.UPDATE_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status) { "Errors: ${context.errors}" }
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test update-card unexpected fail`() = runTest {
        val cardId = CardId("42")
        val testRequestEntity = stubCard.copy(word = "XXX", cardId = cardId)

        val repository = MockDbCardRepository(
            invokeUpdateCard = {
                CardEntityDbResponse(if (it.word == testRequestEntity.word) throw TestException() else testRequestEntity)
            }
        )

        val context = testContext(CardOperation.UPDATE_CARD, repository)
        context.requestCardEntity = testRequestEntity

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.UPDATE_CARD), context.requestId)
        Assertions.assertEquals(CardEntity.EMPTY, context.responseCardEntity)
        assertUnknownError(context, CardOperation.UPDATE_CARD)
    }

    @Test
    fun `test learn-cards success`() = runTest {
        val testLearn = listOf(
            CardLearn(cardId = stubCard.cardId, details = mapOf(Stage.WRITING to 42)),
        )

        val testResponseEntities = listOf(stubCard)

        val repository = MockDbCardRepository(
            invokeLearnCards = {
                CardEntitiesDbResponse(
                    cards = if (it == testLearn) testResponseEntities else emptyList(),
                )
            }
        )

        val context = testContext(CardOperation.LEARN_CARDS, repository)
        context.requestCardLearnList = testLearn

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.LEARN_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test learn-cards error`() = runTest {
        val testLearn = listOf(
            CardLearn(cardId = CardId("1"), details = mapOf(Stage.SELF_TEST to 42)),
            CardLearn(cardId = CardId("2"), details = mapOf(Stage.OPTIONS to 2, Stage.MOSAIC to 3))
        )

        val testResponseEntities = stubCards
        val testResponseErrors = listOf(
            AppError(code = "test")
        )

        val repository = MockDbCardRepository(
            invokeLearnCards = {
                CardEntitiesDbResponse(
                    cards = if (it == testLearn) testResponseEntities else emptyList(),
                    errors = if (it == testLearn) testResponseErrors else emptyList()
                )
            }
        )

        val context = testContext(CardOperation.LEARN_CARDS, repository)
        context.requestCardLearnList = testLearn

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.LEARN_CARDS), context.requestId)
        Assertions.assertEquals(AppStatus.FAIL, context.status)
        Assertions.assertEquals(testResponseErrors, context.errors)
        Assertions.assertEquals(testResponseEntities, context.responseCardEntityList)
    }

    @Test
    fun `test reset-card success`() = runTest {
        val testId = CardId("42")
        val testResponseEntity = stubCard.copy(cardId = testId)

        val repository = MockDbCardRepository(
            invokeResetCard = {
                CardEntityDbResponse(
                    card = if (it == testId) testResponseEntity else CardEntity.EMPTY,
                )
            }
        )

        val context = testContext(CardOperation.RESET_CARD, repository)
        context.requestCardEntityId = testId

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(CardOperation.RESET_CARD), context.requestId)
        Assertions.assertEquals(AppStatus.OK, context.status)
        Assertions.assertTrue(context.errors.isEmpty())
        Assertions.assertEquals(testResponseEntity, context.responseCardEntity)
    }

    @Test
    fun `test delete-card success`() = runTest {
        val testId = CardId("42")
        val response = DeleteEntityDbResponse()

        val repository = MockDbCardRepository(
            invokeDeleteCard = {
                if (it == testId) response else throw TestException()
            }
        )

        val context = testContext(CardOperation.DELETE_CARD, repository)
        context.requestCardEntityId = testId

        CardCorProcessor().execute(context)

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
            word = "xxx",
            translations = listOf(listOf("fff")),
        )
        val testSearchFilter = CardFilter(
            dictionaryIds = listOf(testDictionaryId),
            length = 42,
        )

        val cardRepository = MockDbCardRepository(invokeGetCard = { throw TestException() })
        val userRepository =
            MockDbUserRepository(invokeGetUser = {
                UserEntityDbResponse(
                    user = AppUserEntity.EMPTY,
                    errors = listOf(testError)
                )
            })

        val context = testContext(op, cardRepository = cardRepository, userRepository = userRepository)
        context.requestAppAuthId = testUid
        context.requestCardEntityId = testCardId
        context.requestCardLearnList = listOf(testLearn)
        context.requestCardEntity = testCardEntity
        context.requestCardFilter = testSearchFilter
        context.requestDictionaryId = testDictionaryId

        CardCorProcessor().execute(context)

        Assertions.assertEquals(requestId(op), context.requestId)
        val actual = assertSingleError(context, op)
        Assertions.assertSame(testError, actual)
    }
}