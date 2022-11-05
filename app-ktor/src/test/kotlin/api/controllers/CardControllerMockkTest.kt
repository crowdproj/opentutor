package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.api.services.CardService
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
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
            requestBody = SearchCardsRequest(requestId = "request=cards/search")
        ) { this.searchCards(it) }
    }

    @Test
    fun `test get-all-cards service error`() {
        testServiceError(
            endpoint = "cards/get-all",
            requestBody = GetAllCardsRequest(requestId = "request=cards/get-all")
        ) { this.getAllCards(it) }
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
            requestBody = LearnCardsRequest(requestId = "request=cards/learn")
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

    @Test
    fun `test get-audio-resource service error`() {
        testServiceError(
            endpoint = "sounds/get",
            requestBody = GetAudioRequest(requestId = "request=sounds/get")
        ) { this.getResource(it) }
    }

    private inline fun <reified X : BaseRequest> testServiceError(
        endpoint: String,
        requestBody: X,
        crossinline serviceMethod: suspend CardService.(CardContext) -> CardContext
    ) = testSecuredApp {
        val msg = "for $endpoint"
        val service = mockk<CardService>()
        coEvery {
            service.serviceMethod(any())
        } throws TestException(msg)
        val repositories = mockk<CardRepositories>()

        routing {
            authenticate("auth-jwt") {
                route("test/api") {
                    cards(service, repositories)
                    sounds(service, repositories)
                }
            }
        }
        val response = testPost("/test/api/$endpoint", requestBody)
        val responseBody = response.body<BaseResponse>()
        Assertions.assertEquals(1, responseBody.errors?.size)
        val error = responseBody.errors!![0]
        Assertions.assertEquals("unknown", error.code)
        Assertions.assertEquals("exceptions", error.group)
        Assertions.assertNull(error.field)
        Assertions.assertEquals("Problem with request=${requestBody.requestId} :: $msg", error.message)

        coVerify(exactly = 1) {
            service.serviceMethod(any())
        }
    }
}

internal class TestException(msg: String) : RuntimeException(msg)