package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.testutils.*
import com.gitlab.sszuev.flashcards.api.v1.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DictionarySerializationTest {

    @Test
    fun `test serialization for GetAllDictionariesRequest`() {
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
    fun `test serialization for GetAllDictionariesResponse`() {
        val res1 = GetAllDictionariesResponse(requestId = "request=42", dictionaries = listOf(dictionary))

        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"getAllDictionaries\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        Assertions.assertTrue(json.contains("\"dictionaryId\":\"42\""))
        assertDictionary(json)

        val req2 = deserializeResponse<GetAllDictionariesResponse>(json)
        Assertions.assertNotSame(res1, req2)
        Assertions.assertEquals(res1, req2)
    }

    @Test
    fun `test serialization for DeleteDictionaryRequest`() {
        val req1 = DeleteDictionaryRequest(
            requestId = "request=42",
            dictionaryId = "dictionary=42",
            debug = debug,
        )

        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"requestType\":\"deleteDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        Assertions.assertTrue(json.contains("\"dictionaryId\":\"dictionary=42\""))
        assertDebug(json)

        val req2 = deserializeRequest<DeleteDictionaryRequest>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1, req2)
    }

    @Test
    fun `test serialization for DeleteDictionaryResponse`() {
        val req1 = DeleteDictionaryResponse(
            requestId = "request=42",
            errors = listOf(error),
            result = Result.SUCCESS,
        )

        val json = serialize(req1)
        Assertions.assertTrue(json.contains("\"responseType\":\"deleteDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertError(json)

        val req2 = deserializeResponse<DeleteDictionaryResponse>(json)
        Assertions.assertNotSame(req1, req2)
        Assertions.assertEquals(req1, req2)
    }
}