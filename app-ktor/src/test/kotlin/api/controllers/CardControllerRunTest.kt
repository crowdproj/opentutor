package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DebugResource
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardResponse
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
import com.gitlab.sszuev.flashcards.dbmem.MemDatabase
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.body
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardControllerRunTest {

    @AfterEach
    fun resetDb() {
        MemDatabase.clear()
    }

    @Test
    fun `test get-audio-resource`() = testSecuredApp {
        val requestBody = GetAudioRequest(
            requestId = "resource-request",
            debug = DebugResource(mode = RunMode.TEST),
            word = "xxx",
            lang = "xx",
        )
        val response = testPost("/v1/api/sounds/get", requestBody)
        val res = response.body<GetAudioResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals(requestBody.requestId, res.requestId)
        Assertions.assertEquals(1, res.errors!!.size)
        Assertions.assertEquals(Result.ERROR, res.result)
        Assertions.assertEquals(
            "Error while GET_RESOURCE: no resource found. filter=TTSResourceGet(word=xxx, lang=LangId(id=xx))",
            res.errors!![0].message
        )
        Assertions.assertArrayEquals(ResourceEntity.DUMMY.data, res.resource)
    }

    @Test
    fun `test get-all-cards success`() = testSecuredApp {
        val requestBody = GetAllCardsRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            dictionaryId = "2",
        )
        val response = testPost("/v1/api/cards/get-all", requestBody)
        val res = response.body<GetAllCardsResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.cards)
        Assertions.assertEquals(65, res.cards!!.size)
    }

    @Test
    fun `test get-card success`() = testSecuredApp {
        // deterministic dictionary,we know exactly what entity is coming back:
        val requestBody = GetCardRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            cardId = "246",
        )
        val response = testPost("/v1/api/cards/get", requestBody)
        val res = response.body<GetCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.card)
        Assertions.assertEquals("weather", res.card!!.word)
        Assertions.assertEquals("'weðə", res.card!!.transcription)
        Assertions.assertEquals("noun", res.card!!.partOfSpeech)
        Assertions.assertEquals(listOf(listOf("погода")), res.card!!.translations)
        Assertions.assertEquals(
            listOf(
                "weather forecast -- прогноз погоды",
                "weather bureau -- бюро погоды",
                "nasty weather -- ненастная погода",
                "spell of cold weather -- похолодание"
            ), res.card!!.examples
        )
        Assertions.assertNull(res.card!!.sound)
        Assertions.assertNull(res.card!!.answered)
        Assertions.assertEquals(emptyMap<String, Long>(), res.card!!.details)
    }

    @Test
    fun `test create-card success`() = testSecuredApp {
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
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.card)
        Assertions.assertNotNull(res.card!!.cardId)
        Assertions.assertEquals(expectedCard.dictionaryId.asString(), res.card!!.dictionaryId)
        Assertions.assertEquals(expectedCard.word, res.card!!.word)
        Assertions.assertEquals(expectedCard.transcription, res.card!!.transcription)
        Assertions.assertEquals(expectedCard.translations, res.card!!.translations!!)
        Assertions.assertTrue(res.card!!.examples!!.isEmpty())
    }

    @Test
    fun `test update-card success`() = testSecuredApp {
        val requestBody = UpdateCardRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            card = CardResource(
                cardId = "1",
                dictionaryId = "1",
                word = "climate",
                transcription = "ˈklaɪmɪt",
                translations = listOf(listOf("к-климат")),
                examples = listOf("Create a climate of fear, and it's easy to keep the borders closed."),
                answered = 42,
                details = mapOf("SELF_TEST" to 42L),
                partOfSpeech = "unknown"
            )
        )
        val response = testPost("/v1/api/cards/update", requestBody)
        val res = response.body<UpdateCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.card)
        Assertions.assertNotNull(res.card!!.cardId)
        Assertions.assertEquals(requestBody.card!!, res.card!!)
    }

    @Test
    fun `test search-cards success`() = testSecuredApp {
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
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.cards)
        Assertions.assertEquals(2, res.cards!!.size)
    }

    @Test
    fun `test learn-card success`() = testSecuredApp {
        val requestBody = LearnCardsRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            cards = listOf(LearnResource(cardId = "246", details = mapOf("mosaic" to 42L)))
        )
        val response = testPost("/v1/api/cards/learn", requestBody)
        val res = response.body<LearnCardsResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertEquals(1, res.cards!!.size)
        Assertions.assertEquals("weather", res.cards!![0].word)
    }

    @Test
    fun `test reset-card success`() = testSecuredApp {
        val requestBody = ResetCardRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            cardId = "246",
        )
        val response = testPost("/v1/api/cards/reset", requestBody)
        val res = response.body<ResetCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertEquals("weather", res.card!!.word)
        Assertions.assertEquals(0, res.card!!.answered)
    }

    @Test
    fun `test delete-card success`() = testSecuredApp {
        val requestBody = DeleteCardRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            cardId = "2",
        )
        val response = testPost("/v1/api/cards/delete", requestBody)
        val res = response.body<DeleteCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
    }
}