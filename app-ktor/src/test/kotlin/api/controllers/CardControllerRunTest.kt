package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.testPost
import io.ktor.client.call.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardControllerRunTest {

    @Test
    fun `test get audio resource success`() = testApplication {
        val requestBody = GetAudioRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            word = "xxx",
            lang = "xx",
        )
        val response = testPost("/v1/api/sounds/get", requestBody)
        val res = response.body<GetAudioResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertArrayEquals(ResourceEntity.DUMMY.data, res.resource)
    }

    @Test
    fun `test get-all-cards success`() = testApplication {
        val requestBody = GetAllCardsRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            dictionaryId = "1",
        )
        val response = testPost("/v1/api/cards/get-all", requestBody)
        val res = response.body<GetAllCardsResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertNotNull(res.cards)
        Assertions.assertEquals(65, res.cards!!.size)
    }
}