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

    @Test
    fun `test serialization for DownloadDictionaryRequest`() {
        val res1 = DownloadDictionaryRequest(
            requestId = "request=42",
            dictionaryId = "dictionary=42"
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"requestType\":\"downloadDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        Assertions.assertTrue(json.contains("\"dictionaryId\":\"dictionary=42\""))
        val res2 = deserializeRequest<DownloadDictionaryRequest>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals(res1.requestId, res2.requestId)
    }

    @Test
    fun `test serialization for DownloadDictionaryResponse`() {
        val res1 = DownloadDictionaryResponse(
            resource = byteArrayOf(42),
            result = Result.SUCCESS,
            requestId = "request=42",
            errors = listOf(error)
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"downloadDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        Assertions.assertTrue(json.contains("\"result\":\"success\""))
        assertError(json)
        val res2 = deserializeResponse<DownloadDictionaryResponse>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals(res1.responseType, res2.responseType)
        Assertions.assertEquals(res1.result, res2.result)
        Assertions.assertEquals(res1.requestId, res2.requestId)
        Assertions.assertEquals(res1.errors, res2.errors)
        Assertions.assertArrayEquals(res1.resource, res2.resource)
    }

    @Test
    fun `test serialization for UploadDictionaryRequest`() {
        val res1 = UploadDictionaryRequest(
            resource = byteArrayOf(42),
            requestId = "request=42",
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"requestType\":\"uploadDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        val res2 = deserializeRequest<UploadDictionaryRequest>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertArrayEquals(res1.resource, res2.resource)
    }

    @Test
    fun `test serialization for UploadDictionaryResponse`() {
        val res1 = UploadDictionaryResponse(
            dictionary = dictionary,
            result = Result.SUCCESS,
            requestId = "request=42",
            errors = listOf(error)
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"uploadDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        Assertions.assertTrue(json.contains("\"result\":\"success\""))
        assertError(json)
        assertDictionary(json)
        val res2 = deserializeResponse<UploadDictionaryResponse>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals(res1, res2)
    }

    @Test
    fun `test serialization for CreateDictionaryRequest`() {
        val res1 = CreateDictionaryRequest(
            dictionary = dictionary,
            requestId = "request=42",
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"requestType\":\"createDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertDictionary(json)
        val res2 = deserializeRequest<CreateDictionaryRequest>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals(res1, res2)
    }

    @Test
    fun `test serialization for CreateDictionaryResponse`() {
        val res1 = CreateDictionaryResponse(
            dictionary = dictionary,
            requestId = "request=42",
        )
        val json = serialize(res1)
        Assertions.assertTrue(json.contains("\"responseType\":\"createDictionary\""))
        Assertions.assertTrue(json.contains("\"requestId\":\"request=42\""))
        assertDictionary(json)
        val res2 = deserializeResponse<CreateDictionaryResponse>(json)
        Assertions.assertNotSame(res1, res2)
        Assertions.assertEquals(res1, res2)
    }
}