package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.GetSettingsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetSettingsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.api.v1.models.SettingsResource
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateSettingsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateSettingsResponse
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.body
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class SettingsControllerRunTest {

    @Test
    fun `test get-settings success`() = testSecuredApp {
        val requestBody = GetSettingsRequest(
            requestId = "success-request",
        )
        val response = testPost("/v1/api/settings/get", requestBody)
        val res = response.body<GetSettingsResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.settings)
        Assertions.assertEquals(5, res.settings!!.numberOfWordsPerStage)
        Assertions.assertEquals(6, res.settings!!.stageOptionsNumberOfVariants)
        Assertions.assertEquals(10, res.settings!!.stageShowNumberOfWords)
    }

    @Test
    fun `test update-settings success`() = testSecuredApp {
        val requestBody = UpdateSettingsRequest(
            requestId = "success-request",
            settings = SettingsResource(
                numberOfWordsPerStage = 12,
                stageShowNumberOfWords = 14,
                stageOptionsNumberOfVariants = 13,
            )
        )
        val response = testPost("/v1/api/settings/update", requestBody)
        val res = response.body<UpdateSettingsResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
    }
}