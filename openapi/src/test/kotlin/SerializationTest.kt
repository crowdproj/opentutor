package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.v1.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Acceptance test to control changes in the schema.
 */
internal class SerializationTest {

    companion object {
        private val card = CardResource(
            cardId = "42",
            dictionaryId = "100500",
            word = "XXX",
            transcription = "YYY",
            partOfSpeech = "POS",
            translations = listOf(listOf("a", "b"), listOf("c", "d")),
            examples = listOf("g", "h"),
            answered = 42,
            details = mapOf("A" to 2, "B" to 3),
        )

        private val dictionary = DictionaryResource(
            dictionaryId = "42",
            name = "XXX",
            sourceLang = "X",
            targetLang = "Y",
            partOfSpeech = listOf("X", "Y"),
            total = 1,
            learned = 42
        )

        private val update = CardUpdateResource(
            cardId = "42",
            details = mapOf("A" to 2, "B" to 3),
        )

        private val error = ErrorResource(
            code = "XXX",
            group = "QQQ",
            field = "VVV",
            message = "mmm"
        )

        private val debug = DebugResource(
            mode = RunMode.TEST,
            stub = DebugStub.ERROR_UNKNOWN
        )

        private fun assertCard(json: String) {
            Assertions.assertTrue(json.contains("\"cardId\":\"42\""))
            Assertions.assertTrue(json.contains("\"dictionaryId\":\"100500\""))
            Assertions.assertTrue(json.contains("\"word\":\"XXX\""))
            Assertions.assertTrue(json.contains("\"transcription\":\"YYY\""))
            Assertions.assertTrue(json.contains("\"partOfSpeech\":\"POS\""))
            Assertions.assertTrue(json.contains("\"translations\":[[\"a\",\"b\"],[\"c\",\"d\"]]"))
            Assertions.assertTrue(json.contains("\"examples\":[\"g\",\"h\"]"))
            Assertions.assertTrue(json.contains("\"details\":{\"A\":2,\"B\":3}}"))
            Assertions.assertTrue(json.contains("\"answered\":42"))
            assertDebug(json)
        }

        private fun assertDictionary(json: String) {
            Assertions.assertTrue(json.contains("\"dictionaryId\":\"42\""))
            Assertions.assertTrue(json.contains("\"name\":\"XXX\""))
            Assertions.assertTrue(json.contains("\"sourceLang\":\"X\""))
            Assertions.assertTrue(json.contains("\"targetLang\":\"Y\""))
            Assertions.assertTrue(json.contains("\"partOfSpeech\":[\"X\",\"Y\"]"))
            Assertions.assertTrue(json.contains("\"total\":1"))
            Assertions.assertTrue(json.contains("\"learned\":42"))
        }

        private fun assertUpdate(json: String) {
            Assertions.assertTrue(json.contains("\"cardId\":\"42\""))
            Assertions.assertTrue(json.contains("\"A\":2"))
            Assertions.assertTrue(json.contains("\"B\":3"))
        }

        private fun assertError(json: String) {
            Assertions.assertTrue(json.contains("\"code\":\"XXX\""))
            Assertions.assertTrue(json.contains("\"group\":\"QQQ\""))
            Assertions.assertTrue(json.contains("\"field\":\"VVV\""))
            Assertions.assertTrue(json.contains("\"message\":\"mmm\""))
        }

        private fun assertDebug(json: String) {
            Assertions.assertTrue(json.contains("\"mode\":\"test\""))
            Assertions.assertTrue(json.contains("\"stub\":\"error_unknown\""))
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
        Assertions.assertTrue(json.contains("\"requestType\":\"createCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertCard(json)
        val req2 = deserializeRequest<CreateCardRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(res1, res2)
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
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(res1, res2)
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
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertTrue(json.contains("\"result\":\"error\""))
        assertError(json)
        assertDictionary(json)

        val req1 = deserializeResponse<GetAllDictionariesResponse>(json)
        Assertions.assertNotSame(res1, req1)
        Assertions.assertEquals(res1, req1)
    }

    @Test
    fun `test serialization for LearnCardRequest`() {
        val req1 = LearnCardRequest(cards = listOf(update), requestId = "request=42")
        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"learnCard\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertUpdate(json)

        val req2 = deserializeRequest<LearnCardRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1, req2)
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
        Assertions.assertEquals(res1.responseType, res2.responseType)
        Assertions.assertEquals(res1.result, res2.result)
        Assertions.assertEquals(res1.requestId, res2.requestId)
        Assertions.assertEquals(res1.errors, res2.errors)
        Assertions.assertArrayEquals(res1.resource, res2.resource)
    }
}