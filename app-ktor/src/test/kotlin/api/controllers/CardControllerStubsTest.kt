package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordResource
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DebugResource
import com.gitlab.sszuev.flashcards.api.v1.models.DebugStub
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.ErrorResource
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.LearnCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.LearnResource
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.api.v1.models.RunMode
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardResponse
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.stubLearnCardDetails
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CardControllerStubsTest {

    companion object {
        private val testCard = CardResource(
            words = stubCard.words.map { CardWordResource(word = it.word) },
            cardId = stubCard.cardId.asString(),
            dictionaryId = stubCard.dictionaryId.asString()
        )
        private val testLearnCard = LearnResource(
            cardId = stubLearnCardDetails.cardId.asString(),
            details = stubLearnCardDetails.details.mapKeys { it.toString() },
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
            Assertions.assertEquals(actual.words.size, expected.words!!.size)
            actual.words.forEachIndexed { index, word ->
                Assertions.assertEquals(word.word, expected.words!![index].word)
            }
        }
    }

    @Test
    fun `test create-card success`() = testSecuredApp {
        val requestBody = CreateCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            card = testCard
        )
        val response = testPost("/v1/api/cards/create", requestBody)
        testResponseSuccess<CreateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test create-card error`() = testSecuredApp {
        val requestBody = CreateCardRequest(
            requestId = "error-request",
            debug = debugError,
            card = testCard
        )
        val response = testPost("/v1/api/cards/create", requestBody)
        testResponseError<CreateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test update-card success`() = testSecuredApp {
        val requestBody = UpdateCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            card = testCard
        )
        val response = testPost("/v1/api/cards/update", requestBody)
        testResponseSuccess<UpdateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test update-card error`() = testSecuredApp {
        val requestBody = UpdateCardRequest(
            requestId = "error-request",
            debug = debugError,
            card = testCard
        )
        val response = testPost("/v1/api/cards/update", requestBody)
        testResponseError<UpdateCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test search-cards card success`() = testSecuredApp {
        val requestBody = SearchCardsRequest(
            requestId = "success-request",
            debug = debugSuccess,
            dictionaryIds = listOf("42"),
            length = 1,
            random = false,
            unknown = true,
        )
        val response = testPost("/v1/api/cards/search", requestBody)
        val responseBody = testResponseSuccess<SearchCardsResponse>(requestBody.requestId, response)
        Assertions.assertEquals(stubCards.size, responseBody.cards!!.size)
        responseBody.cards!!.forEachIndexed { index, cardResource ->
            val card = stubCards[index]
            assertCard(card, cardResource)
        }
    }

    @Test
    fun `test search-cards error`() = testSecuredApp {
        val requestBody = SearchCardsRequest(
            requestId = "error-request",
            debug = debugError,
            dictionaryIds = listOf("a", "b", "c"),
            length = 42,
            random = true,
            unknown = false,
        )
        val response = testPost("/v1/api/cards/search", requestBody)
        testResponseError<SearchCardsResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test get-card success`() = testSecuredApp {
        val requestBody = GetCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cardId = testCard.cardId,
        )
        val response = testPost("/v1/api/cards/get", requestBody)
        val responseBody = testResponseSuccess<GetCardResponse>(requestBody.requestId, response)
        Assertions.assertNotNull(responseBody.card)
        val responseEntity = responseBody.card!!
        assertCard(stubCard, responseEntity)
    }

    @Test
    fun `test get-card error`() = testSecuredApp {
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
    fun `test cards-learn success`() = testSecuredApp {
        val requestBody = LearnCardsRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cards = listOf(testLearnCard.copy(cardId = "a"), testLearnCard.copy(cardId = "b")),
        )
        val response = testPost("/v1/api/cards/learn", requestBody)
        testResponseSuccess<LearnCardsResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test cards-learn error`() = testSecuredApp {
        val requestBody = LearnCardsRequest(
            requestId = "error-request",
            debug = debugError,
            cards = listOf(testLearnCard.copy(cardId = "d"), testLearnCard.copy(cardId = "e")),
        )
        val response = testPost("/v1/api/cards/learn", requestBody)
        testResponseError<LearnCardsResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test reset-card success`() = testSecuredApp {
        val requestBody = ResetCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/reset", requestBody)
        testResponseSuccess<ResetCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test reset-card error`() = testSecuredApp {
        val requestBody = ResetCardRequest(
            requestId = "error-request",
            debug = debugError,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/reset", requestBody)
        testResponseError<ResetCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test delete-card success`() = testSecuredApp {
        val requestBody = DeleteCardRequest(
            requestId = "success-request",
            debug = debugSuccess,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/delete", requestBody)
        testResponseSuccess<DeleteCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test delete-card error`() = testSecuredApp {
        val requestBody = DeleteCardRequest(
            requestId = "error-request",
            debug = debugError,
            cardId = "42",
        )
        val response = testPost("/v1/api/cards/delete", requestBody)
        testResponseError<DeleteCardResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test get-audio resource error`() = testSecuredApp {
        val requestBody = GetAudioRequest(
            requestId = "error-request",
            debug = debugError,
            word = "xxx",
            lang = "xx",
        )
        val response = testPost("/v1/api/sounds/get", requestBody)
        testResponseError<GetAudioResponse>(requestBody.requestId, response)
    }

    @Test
    fun `test get-audio resource success`() = testSecuredApp {
        val requestBody = GetAudioRequest(
            requestId = "success-request",
            debug = debugSuccess,
            word = "xxx",
            lang = "xx",
        )
        val response = testPost("/v1/api/sounds/get", requestBody)
        testResponseSuccess<GetAudioResponse>(requestBody.requestId, response)
    }


    @Test
    fun `test get-all-cards card success`() = testSecuredApp {
        val requestBody = GetAllCardsRequest(
            requestId = "success-request",
            debug = debugSuccess,
            dictionaryId = "42",
        )
        val response = testPost("/v1/api/cards/get-all", requestBody)
        val responseBody = testResponseSuccess<GetAllCardsResponse>(requestBody.requestId, response)
        Assertions.assertEquals(stubCards.size, responseBody.cards!!.size)
        responseBody.cards!!.forEachIndexed { index, cardResource ->
            val card = stubCards[index]
            assertCard(card, cardResource)
        }
    }

    @Test
    fun `test get-all-cards error`() = testSecuredApp {
        val requestBody = GetAllCardsRequest(
            requestId = "error-request",
            debug = debugError,
            dictionaryId = "XXX",
        )
        val response = testPost("/v1/api/cards/get-all", requestBody)
        testResponseError<GetAllCardsResponse>(requestBody.requestId, response)
    }
}
