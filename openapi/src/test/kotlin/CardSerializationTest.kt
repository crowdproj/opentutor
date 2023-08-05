package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.testutils.assertDebug
import com.gitlab.sszuev.flashcards.api.testutils.assertDictionary
import com.gitlab.sszuev.flashcards.api.testutils.assertError
import com.gitlab.sszuev.flashcards.api.testutils.debug
import com.gitlab.sszuev.flashcards.api.testutils.deserializeRequest
import com.gitlab.sszuev.flashcards.api.testutils.deserializeResponse
import com.gitlab.sszuev.flashcards.api.testutils.dictionary
import com.gitlab.sszuev.flashcards.api.testutils.error
import com.gitlab.sszuev.flashcards.api.testutils.normalize
import com.gitlab.sszuev.flashcards.api.testutils.serialize
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordExampleResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordResource
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnResource
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsResponse
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Acceptance test to control changes in the schema.
 */
internal class CardSerializationTest {

    companion object {
        private val card = CardResource(
            cardId = "42",
            dictionaryId = "100500",
            words = listOf(
                CardWordResource(
                    word = "XXX",
                    transcription = "YYY",
                    partOfSpeech = "POS",
                    translations = listOf(listOf("a", "b"), listOf("c", "d")),
                    examples = listOf(
                        CardWordExampleResource(example = "g"), CardWordExampleResource(example = "h")
                    ),
                )
            ),
            answered = 42,
            details = mapOf("A" to 2, "B" to 3),
            stats = mapOf("C" to 42, "D" to -42),
            changedAt = OffsetDateTime.of(LocalDate.of(42, 4, 2), LocalTime.of(4, 2), ZoneOffset.UTC).plusDays(42)
        )

        private val cardJson = """
          "card": {
            "cardId": "42",
            "dictionaryId": "100500",
            "words": [
              {
                "word": "XXX",
                "transcription": "YYY",
                "partOfSpeech": "POS",
                "translations": [
                  [
                    "a",
                    "b"
                  ],
                  [
                    "c",
                    "d"
                  ]
                ],
                "examples": [
                  {
                    "example": "g"
                  },
                  {
                    "example": "h"
                  }
                ]
              }
            ],
            "stats": {
              "C": 42,
              "D": -42
            },
            "details": {
              "A": 2,
              "B": 3
            },
            "answered": 42,
            "changedAt": -60830251080.000000000
          }
        """.normalize()

        private val update = LearnResource(
            cardId = "42",
            details = mapOf("A" to 2, "B" to 3),
        )

        private fun assertCard(json: String) {
            Assertions.assertTrue(json.contains(cardJson))
            assertDebug(json)
        }

        private fun assertUpdate(json: String) {
            Assertions.assertTrue(json.contains("\"cardId\":\"42\""))
            Assertions.assertTrue(json.contains("\"A\":2"))
            Assertions.assertTrue(json.contains("\"B\":3"))
        }
    }

    @Test
    fun `test serialization for CreateCardRequest`() {
        val req1 = CreateCardRequest(
            card = card,
            requestId = "request=42",
            debug = debug
        )
        val json = serialize(req1)
        Assertions.assertEquals(
            """
            {
              "requestType": "createCard",
              "requestId": "request=42",
              "debug": {
                "mode": "test",
                "stub": "error_unknown"
              },
              "card": {
                "cardId": "42",
                "dictionaryId": "100500",
                "words": [
                  {
                    "word": "XXX",
                    "transcription": "YYY",
                    "partOfSpeech": "POS",
                    "translations": [
                      [
                        "a",
                        "b"
                      ],
                      [
                        "c",
                        "d"
                      ]
                    ],
                    "examples": [
                      {
                        "example": "g"
                      },
                      {
                        "example": "h"
                      }
                    ]
                  }
                ],
                "stats": {
                  "C": 42,
                  "D": -42
                },
                "details": {
                  "A": 2,
                  "B": 3
                },
                "answered": 42,
                "changedAt": -60830251080.000000000
              }
            }
        """.normalize(), json)
        val req2 = deserializeRequest<CreateCardRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "createCard"), req2)
    }

    @Test
    fun `test serialization for UpdateCardRequest`() {
        val req1 = UpdateCardRequest(
            card = card,
            requestId = "request=42",
            debug = debug
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"updateCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertCard(json)
        val req2 = deserializeRequest<UpdateCardRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "updateCard"), req2)
    }

    @Test
    fun `test serialization for SearchCardsRequest`() {
        val req1 = SearchCardsRequest(
            random = false,
            unknown = true,
            length = 42,
            dictionaryIds = listOf("100500", "4200"),
            requestId = "request=42",
            debug = debug
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"searchCards\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val req2 = deserializeRequest<SearchCardsRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "searchCards"), req2)
    }

    @Test
    fun `test serialization for SearchCardsResponse`() {
        val res1 = SearchCardsResponse(
            cards = listOf(card, card),
            requestId = "request=42",
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"searchCards\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val res2 = deserializeResponse<SearchCardsResponse>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals(res1.copy(responseType = "searchCards"), res2)
    }

    @Test
    fun `test serialization for GetAllCardsRequest`() {
        val req1 = GetAllCardsRequest(
            dictionaryId = "42",
            requestId = "request=42",
            debug = debug
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"getAllCards\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val req2 = deserializeRequest<GetAllCardsRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "getAllCards"), req2)
    }

    @Test
    fun `test serialization for GetAllCardsResponse`() {
        val res1 = GetAllCardsResponse(
            cards = listOf(card, card),
            requestId = "request=42",
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"getAllCards\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val res2 = deserializeResponse<GetAllCardsResponse>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals(res1.copy(responseType = "getAllCards"), res2)
    }

    @Test
    fun `test serialization for GetCardRequest`() {
        val req1 = GetCardRequest(
            cardId = "card-42",
            requestId = "request=42",
            debug = debug
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"getCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val req2 = deserializeRequest<GetCardRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "getCard"), req2)
    }

    @Test
    fun `test serialization for ResetCardRequest`() {
        val req1 = ResetCardRequest(
            cardId = "card-42",
            requestId = "request=42",
            debug = debug
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"resetCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val req2 = deserializeRequest<ResetCardRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "resetCard"), req2)
    }

    @Test
    fun `test serialization for DeleteCardRequest`() {
        val req1 = DeleteCardRequest(
            cardId = "card-42",
            requestId = "request=42",
            debug = debug
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"deleteCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val req2 = deserializeRequest<DeleteCardRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "deleteCard"), req2)
    }

    @Test
    fun `test serialization for DeleteCardResponse`() {
        val req1 = DeleteCardResponse(
            requestId = "request=42",
            errors = listOf(error)
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"responseType\":\"deleteCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertError(json)
        val req2 = deserializeResponse<DeleteCardResponse>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(responseType = "deleteCard"), req2)
    }

    @Test
    fun `test serialization for GetAllDictionaryRequest`() {
        val req1 = GetAllDictionariesRequest(
            requestId = "request=42",
        )
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"getAllDictionaries\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))

        val req2 = deserializeRequest<GetAllDictionariesRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "getAllDictionaries"), req2)
    }

    @Test
    fun `test serialization for GetAllDictionaryResponse`() {
        val res1 = GetAllDictionariesResponse(
            dictionaries = listOf(dictionary),
            result = Result.ERROR,
            requestId = "request=42",
            errors = listOf(error)
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"getAllDictionaries\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertError(json)
        assertDictionary(json)

        val req1 = deserializeResponse<GetAllDictionariesResponse>(json)
        Assertions.assertNotSame(res1, req1)
        Assertions.assertEquals(res1.copy(responseType = "getAllDictionaries"), req1)
    }

    @Test
    fun `test serialization for LearnCardsRequest`() {
        val req1 = LearnCardsRequest(cards = listOf(update), requestId = "request=42")
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"learnCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertUpdate(json)

        val req2 = deserializeRequest<LearnCardsRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1.copy(requestType = "learnCard"), req2)
    }

    @Test
    fun `test serialization for GetAudioResponse`() {
        val res1 = GetAudioResponse(
            resource = byteArrayOf(42),
            result = Result.SUCCESS,
            requestId = "request=42",
            errors = listOf(error)
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"getAudio\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        Assertions.assertTrue(json.contains("\"result\":\"success\""))
        assertError(json)
        val res2 = deserializeResponse<GetAudioResponse>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals("getAudio", res2.responseType)
        Assertions.assertEquals(res1.result, res2.result)
        Assertions.assertEquals(res1.requestId, res2.requestId)
        Assertions.assertEquals(res1.errors, res2.errors)
        Assertions.assertArrayEquals(res1.resource, res2.resource)
    }
}