package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.stubLearnCardDetails
import com.gitlab.sszuev.flashcards.testPost
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CardControllerStubsTest {

    companion object {
        private val testCard = CardResource(
            word = stubCard.word,
            cardId = stubCard.cardId.asString(),
            dictionaryId = stubCard.dictionaryId.asString()
        )
        private val testLearnCard = CardUpdateResource(
            cardId = stubLearnCardDetails.cardId.asString(),
            details = stubLearnCardDetails.details,
        )
        private val debugSuccess = DebugResource(
            mode = RunMode.STUB,
            stub = DebugStub.SUCCESS
        )
        private val debugError = DebugResource(
            mode = RunMode.STUB,
            stub = DebugStub.ERROR_UNKNOWN
        )

        private suspend inline fun <reified X : BaseResponse> testResponseSuccess(
            requestId: String?,
            response: HttpResponse
        ): X {
            val res = response.body<X>()
            Assertions.assertEquals(200, response.status.value)
            Assertions.assertEquals(requestId, res.requestId)
            Assertions.assertEquals(Result.SUCCESS, res.result)
            Assertions.assertNull(res.errors)
            return res
        }

        private suspend inline fun <reified X : BaseResponse> testResponseError(
            requestId: String?,
            response: HttpResponse
        ): X {
            val res = response.body<X>()
            Assertions.assertEquals(200, response.status.value)
            Assertions.assertEquals(requestId, res.requestId)
            Assertions.assertEquals(Result.ERROR, res.result)
            Assertions.assertNotNull(res.errors)
            asserError(res.errors)
            return res
        }

        private fun asserError(errors: List<ErrorResource>?) {
            Assertions.assertNotNull(errors)
            errors?.let { Assertions.assertEquals(1, it.size) }
            val error = errors!![0]
            Assertions.assertEquals(stubError.field, error.field)
            Assertions.assertEquals(stubError.message, error.message)
            Assertions.assertEquals(stubError.code, error.code)
            Assertions.assertEquals(stubError.group, error.group)
        }

        private fun assertCard(actual: CardEntity, expected: CardResource) {
            Assertions.assertEquals(actual.cardId.asString(), expected.cardId)
            Assertions.assertEquals(actual.dictionaryId.asString(), expected.dictionaryId)
            Assertions.assertEquals(actual.word, expected.word)
        }
    }

    @Test
    fun `test create-card success`() = testApplication {
        val requestBody = CreateCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            card = testCard
        )
        val response = testPost("/v1/api/cards/create", requestBody)
        testResponseSuccess<CreateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test create-card error`() = testApplication {
        val requestBody = CreateCardRequest(
            requestId = "error-request",
            debug = debugError,
            card = testCard
        )
        val response = testPost("/v1/api/cards/create", requestBody)
        testResponseError<CreateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test update-card success`() = testApplication {
        val requestBody = UpdateCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            card = testCard
        )
        val response = testPost("/v1/api/cards/update", requestBody)
        testResponseSuccess<UpdateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test update-card error`() = testApplication {
        val requestBody = UpdateCardRequest(
            requestId = "error-request",
            debug = debugError,
            card = testCard
        )
        val response = testPost("/v1/api/cards/update", requestBody)
        testResponseError<UpdateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test search-cards card success`() = testApplication {
        val requestBody = GetCardsRequest(
            requestId = "success-request",
            debug = debugSuccess,
            dictionaryIds = listOf("42"),
            length = 1,
            random = false,
            unknown = true,
        )
        val response = testPost("/v1/api/cards/search", requestBody)
        val responseBody = testResponseSuccess<GetCardsResponse>(requestBody.requestId, response)
        Assertions.assertEquals(stubCards.size, responseBody.cards!!.size)
        responseBody.cards!!.forEachIndexed { index, cardResource ->
            val card = stubCards[index]
            assertCard(card, cardResource)
        }
    }

    @Test
    fun `test search-cards error`() = testApplication {
        val requestBody = GetCardsRequest(
            requestId = "error-request",
            debug = debugError,
            dictionaryIds = listOf("a", "b", "c"),
            length = 42,
            random = true,
            unknown = false,
        )
        val response = testPost("/v1/api/cards/search", requestBody)
        testResponseError<GetCardsResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test get-card success`() = testApplication {
        val requestBody = GetCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cardId = testCard.cardId,
        )
        val response = testPost("/v1/api/cards/get", requestBody)
        val responseBody = testResponseSuccess<GetCardResponse>(requestBody.requestId, response)
        Assertions.assertNotNull(responseBody.card)
        val responseEntity = responseBody.card!!
        Assertions.assertNotSame(stubCard, responseEntity)
    }

    @Test
    fun `test get-card error`() = testApplication {
        val requestBody = GetCardRequest(
            requestId = "error-request",
            debug = debugError,
            cardId = testCard.cardId,
        )
        val response = testPost("/v1/api/cards/get", requestBody)
        val responseBody = testResponseError<GetCardResponse>(requestBody.requestId, response)
        Assertions.assertNull(responseBody.card)
    }

    @Test
    fun `test cards-learn success`() = testApplication {
        val requestBody = LearnCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cards = listOf(testLearnCard.copy(cardId = "a"), testLearnCard.copy(cardId = "b")),
        )
        val response = testPost("/v1/api/cards/learn", requestBody)
        testResponseSuccess<LearnCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test cards-learn error`() = testApplication {
        val requestBody = LearnCardRequest(
            requestId = "error-request",
            debug = debugError,
            cards = listOf(testLearnCard.copy(cardId = "d"), testLearnCard.copy(cardId = "e")),
        )
        val response = testPost("/v1/api/cards/learn", requestBody)
        testResponseError<LearnCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test reset-card success`() = testApplication {
        val requestBody = ResetCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/reset", requestBody)
        testResponseSuccess<ResetCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test reset-card error`() = testApplication {
        val requestBody = ResetCardRequest(
            requestId = "error-request",
            debug = debugError,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/reset", requestBody)
        testResponseError<ResetCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test delete-card success`() = testApplication {
        val requestBody = DeleteCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/delete", requestBody)
        testResponseSuccess<DeleteCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test delete-card error`() = testApplication {
        val requestBody = DeleteCardRequest(
            requestId = "error-request",
            debug = debugError,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/delete", requestBody)
        testResponseError<DeleteCardResponse>(requestBody.requestId, response)
    }
}
