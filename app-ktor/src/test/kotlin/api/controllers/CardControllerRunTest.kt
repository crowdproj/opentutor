package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordExampleResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordResource
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardResponse
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
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardResponse
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.body
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.time.OffsetDateTime
import java.time.ZoneOffset

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class CardControllerRunTest {

    companion object {
        private fun CardResource.word() = this.words!!.single().word
        private fun CardResource.translations() = this.words!!.single().translations
        private fun CardResource.transcription() = this.words!!.single().transcription
    }

    @Order(1)
    @Test
    fun `test get-audio-resource`() = testSecuredApp {
        val requestBody = GetAudioRequest(
            requestId = "resource-request",
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

    @Order(2)
    @Test
    fun `test get-all-cards success`() = testSecuredApp {
        val requestBody = GetAllCardsRequest(
            requestId = "success-request",
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

    @Order(3)
    @Test
    fun `test get-card success`() = testSecuredApp {
        // deterministic dictionary, we know exactly what entity is coming back:
        val requestBody = GetCardRequest(
            requestId = "success-request",
            cardId = "246",
        )
        val response = testPost("/v1/api/cards/get", requestBody)
        val res = response.body<GetCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.card)
        Assertions.assertEquals("weather", res.card!!.words!!.single().word)
        Assertions.assertEquals("'weðə", res.card!!.words!!.single().transcription)
        Assertions.assertEquals("noun", res.card!!.words!!.single().partOfSpeech)
        Assertions.assertEquals(listOf(listOf("погода")), res.card!!.words!!.single().translations)
        Assertions.assertEquals(
            listOf(
                "weather forecast",
                "weather bureau",
                "nasty weather",
                "spell of cold weather"
            ), res.card!!.words!!.single().examples?.map { it.example }
        )
        Assertions.assertEquals("en:weather", res.card!!.words!!.single().sound)
        Assertions.assertNull(res.card!!.answered)
        Assertions.assertEquals(emptyMap<String, Long>(), res.card!!.details)
    }

    @Order(4)
    @Test
    fun `test create-card success`() = testSecuredApp {
        val expectedCard = CardEntity(
            dictionaryId = DictionaryId("2"),
            words = listOf(
                CardWordEntity(
                    word = "rainy",
                    transcription = "ˈreɪnɪ",
                    translations = listOf(listOf("дождливый")),
                )
            ),
        )
        val requestBody = CreateCardRequest(
            requestId = "success-request",
            card = CardResource(
                dictionaryId = expectedCard.dictionaryId.asString(),
                words = expectedCard.words.map {
                    CardWordResource(
                        word = it.word,
                        transcription = it.transcription,
                        partOfSpeech = it.partOfSpeech,
                        translations = it.translations
                    )
                },
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
        Assertions.assertEquals(expectedCard.words.single().word, res.card!!.word())
        Assertions.assertEquals(expectedCard.words.single().transcription, res.card!!.transcription())
        Assertions.assertEquals(expectedCard.words.single().translations, res.card!!.translations())
        Assertions.assertTrue(res.card!!.words!!.single().examples!!.isEmpty())
    }

    @Order(5)
    @Test
    fun `test search-cards success`() = testSecuredApp {
        val requestBody = SearchCardsRequest(
            requestId = "success-request",
            dictionaryIds = listOf("2"),
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

    @Order(6)
    @Test
    fun `test learn-card success`() = testSecuredApp {
        val requestBody = LearnCardsRequest(
            requestId = "success-request",
            cards = listOf(LearnResource(cardId = "246", details = mapOf("mosaic" to 42L)))
        )
        val response = testPost("/v1/api/cards/learn", requestBody)
        val res = response.body<LearnCardsResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertEquals(1, res.cards!!.size)
        Assertions.assertEquals("weather", res.cards!![0].word())
    }

    @Order(7)
    @Test
    fun `test reset-card success`() = testSecuredApp {
        val requestBody = ResetCardRequest(
            requestId = "success-request",
            cardId = "246",
        )
        val response = testPost("/v1/api/cards/reset", requestBody)
        val res = response.body<ResetCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertEquals("weather", res.card!!.word())
        Assertions.assertEquals(0, res.card!!.answered)
    }

    @Order(8)
    @Test
    fun `test update-card success`() = testSecuredApp {
        val start = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        val requestBody = UpdateCardRequest(
            requestId = "success-request",
            card = CardResource(
                cardId = "246",
                dictionaryId = "2",
                words = listOf(
                    CardWordResource(
                        word = "climate",
                        transcription = "ˈklaɪmɪt",
                        translations = listOf(listOf("к-климат")),
                        examples = listOf(
                            CardWordExampleResource("Create a climate of fear, and it's easy to keep the borders closed.")
                        ),
                        partOfSpeech = "unknown"
                    )
                ),
                answered = 42,
                stats = mapOf("self-test" to 42L),
            )
        )
        val response = testPost("/v1/api/cards/update", requestBody)
        val end = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1)
        val responseBody = response.body<UpdateCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertNull(responseBody.errors) { "Has errors: ${responseBody.errors}" }
        Assertions.assertEquals("success-request", responseBody.requestId)
        Assertions.assertEquals(Result.SUCCESS, responseBody.result)
        Assertions.assertNotNull(responseBody.card)
        Assertions.assertNotNull(responseBody.card!!.cardId)
        Assertions.assertEquals(
            requestBody.card!!.copy(
                details = emptyMap(),
                words = listOf(requestBody.card!!.words!!.single().copy(sound = "en:climate", primary = true)),
            ),
            responseBody.card!!.copy(changedAt = null)
        )
        Assertions.assertEquals(0, responseBody.card!!.details!!.size)
        val actualChangedAt = responseBody.card!!.changedAt!!
        Assertions.assertTrue(actualChangedAt in start..end)
    }

    @Order(9)
    @Test
    fun `test delete-card success`() = testSecuredApp {
        val requestBody = DeleteCardRequest(
            requestId = "success-request",
            cardId = "246",
        )
        val response = testPost("/v1/api/cards/delete", requestBody)
        val res = response.body<DeleteCardResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
    }
}