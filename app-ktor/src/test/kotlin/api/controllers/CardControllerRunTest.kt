package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
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

    @Test
    fun `test create-card success`() = testApplication {
        val expectedCard = CardEntity(
            dictionaryId = DictionaryId("1"),
            word = "rainy",
            transcription = "ˈreɪnɪ",
            translations = listOf(listOf("дождливый")),
        )
        val requestBody = CreateCardRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            card = CardResource(
                dictionaryId = expectedCard.dictionaryId.asString(),
                word = expectedCard.word,
                transcription = expectedCard.transcription,
                translations = expectedCard.translations,
            )
        )
        val response = testPost("/v1/api/cards/create", requestBody)
        val res = response.body<CreateCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertNotNull(res.card)
        Assertions.assertNotNull(res.card!!.cardId)
        Assertions.assertEquals(expectedCard.dictionaryId.asString(), res.card!!.dictionaryId)
        Assertions.assertEquals(expectedCard.word, res.card!!.word)
        Assertions.assertEquals(expectedCard.transcription, res.card!!.transcription)
        Assertions.assertEquals(expectedCard.translations, res.card!!.translations!!)
        Assertions.assertTrue(res.card!!.examples!!.isEmpty())
    }

    @Test
    fun `test search-cards success`() = testApplication {
        val requestBody = SearchCardsRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            dictionaryIds = listOf("1", "2"),
            random = false,
            length = 2,
        )
        val response = testPost("/v1/api/cards/search", requestBody)
        val res = response.body<SearchCardsResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNull(res.errors)
        Assertions.assertNotNull(res.cards)
        Assertions.assertEquals(2, res.cards!!.size)
    }
}