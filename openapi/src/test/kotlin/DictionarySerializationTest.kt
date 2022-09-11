package com.gitlab.sszuev.flashcards.api

import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesResponse
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
        val res1 = GetAllDictionariesResponse(
            requestId = "request=42",
            dictionaries = listOf(
                DictionaryResource(
                    dictionaryId = "42",
                    name = "Test-dictionary",
                    partsOfSpeech = listOf("verb", "noun"),
                    sourceLang = "XX",
                    targetLang = "YY",
                )
            )
        )

        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"getAllDictionaries\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        Assertions.assertTrue(json.contains("\"dictionaryId\":\"42\""))

        val req2 = deserializeResponse<GetAllDictionariesResponse>(json)
        Assertions.assertNotSame(res1, req2)
        Assertions.assertEquals(res1, req2)
    }
}