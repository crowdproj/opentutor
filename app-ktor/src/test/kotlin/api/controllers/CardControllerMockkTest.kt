package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.testPost
import io.ktor.client.call.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardControllerMockkTest {

    @Test
    fun `test get-card service error`() {
        testServiceError(
            endpoint = "cards/get",
            requestBody = GetCardRequest(requestId = "request=cards/get")
        ) { this.getCard(it) }
    }

    @Test
    fun `test search-cards service error`() {
        testServiceError(
            endpoint = "cards/search",
            requestBody = GetCardsRequest(requestId = "request=cards/search")
        ) { this.searchCards(it) }
    }

    @Test
    fun `test create-card service error`() {
        testServiceError(
            endpoint = "cards/create",
            requestBody = CreateCardRequest(requestId = "request=cards/create")
        ) { this.createCard(it) }
    }

    @Test
    fun `test update-card service error`() {
        testServiceError(
            endpoint = "cards/update",
            requestBody = UpdateCardRequest(requestId = "request=cards/update")
        ) { this.updateCard(it) }
    }

    @Test
    fun `test learn-card service error`() {
        testServiceError(
            endpoint = "cards/learn",
            requestBody = LearnCardRequest(requestId = "request=cards/learn")
        ) { this.learnCard(it) }
    }

    @Test
    fun `test reset-card service error`() {
        testServiceError(
            endpoint = "cards/reset",
            requestBody = ResetCardRequest(requestId = "request=cards/reset")
        ) { this.resetCard(it) }
    }

    @Test
    fun `test delete-card service error`() {
        testServiceError(
            endpoint = "cards/delete",
            requestBody = DeleteCardRequest(requestId = "request=cards/delete")
        ) { this.deleteCard(it) }
    }

    private inline fun <reified X : BaseRequest> testServiceError(
        endpoint: String,
        requestBody: X,
        crossinline serviceMethod: suspend CardService.(CardContext) -> CardContext
    ) = testApplication {
        val msg = "for $endpoint"
        val service = mockk<CardService>()
        coEvery {
            service.serviceMethod(any())
        } throws AssertionError(msg)

        routing {
            route("test/api") {
                cards(service)
            }
        }
        val response = testPost("/test/api/$endpoint", requestBody)
        val responseBody = response.body<BaseResponse>()
        Assertions.assertEquals(1, responseBody.errors?.size)
        val error = responseBody.errors!![0]
        println(error.message)
        Assertions.assertEquals("unknown", error.code)
        Assertions.assertEquals("exceptions", error.group)
        Assertions.assertNull(error.field)
        Assertions.assertEquals("Problem with request=${requestBody.requestId} :: $msg", error.message)

        coVerify(exactly = 1) {
            service.serviceMethod(any())
        }
    }

}