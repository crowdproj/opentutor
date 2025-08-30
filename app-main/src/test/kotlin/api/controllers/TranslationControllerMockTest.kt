package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.FetchTranslationRequest
import com.gitlab.sszuev.flashcards.api.v1.models.FetchTranslationResponse
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.config.TutorConfig
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.services.TranslationService
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.body
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.route
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class TranslationControllerMockTest {

    @Test
    fun `test fetch translation service success`() = testSecuredApp {
        val endpoint = "translation/fetch"
        val requestBody = FetchTranslationRequest(requestId = "translation/fetch")

        var serviceCallCount = 0
        val service = TestTranslationService { context ->
            serviceCallCount++
            context.responseCardEntity = CardEntity(
                words = listOf(CardWordEntity(word = "qqq", translations = listOf(listOf("xxx"))))
            )
            return@TestTranslationService context
        }

        val tutorConfig = mockk<TutorConfig>(relaxed = true)
        val runConfig = mockk<RunConfig>(relaxed = true)
        val contextConfig = ContextConfig(runConfig, tutorConfig)

        routing {
            authenticate("auth-jwt") {
                route("test/api") {
                    translation(service, contextConfig)
                }
            }
        }
        val response = testPost("/test/api/$endpoint", requestBody)
        val responseBody = response.body<BaseResponse>()

        Assertions.assertInstanceOf(FetchTranslationResponse::class.java, responseBody)
        Assertions.assertEquals(1, serviceCallCount)
        Assertions.assertNull(responseBody.errors)
        responseBody as FetchTranslationResponse
        val actualCard = responseBody.card!!
        Assertions.assertEquals("qqq", actualCard.words?.single()?.word)
        Assertions.assertEquals(listOf(listOf("xxx")), actualCard.words?.single()?.translations)
    }

    private class TestTranslationService(
        private val handleContext: (TranslationContext) -> TranslationContext
    ) : TranslationService {
        override suspend fun fetchTranslation(context: TranslationContext): TranslationContext = handleContext(context)
    }
}