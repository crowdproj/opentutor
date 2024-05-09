package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioRequest
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.config.TutorConfig
import com.gitlab.sszuev.flashcards.services.TTSService
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.body
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.route
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TTSControllerMockTest {

    @Test
    fun `test get-audio-resource service error`() = testSecuredApp {
        val endpoint = "sounds/get"
        val requestBody = GetAudioRequest(requestId = "request=sounds/get")

        val msg = "for $endpoint"
        val service = mockk<TTSService>()
        coEvery {
            service.getResource(any())
        } throws RuntimeException(msg)

        val tutorConfig = mockk<TutorConfig>(relaxed = true)
        val runConfig = mockk<RunConfig>(relaxed = true)
        val contextContext = ContextConfig(runConfig, tutorConfig)

        routing {
            authenticate("auth-jwt") {
                route("test/api") {
                    sounds(service, contextContext)
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
            service.getResource(any())
        }
    }
}
