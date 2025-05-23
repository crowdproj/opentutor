package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.CreateDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CreateDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.DownloadDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DownloadDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesResponse
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateDictionaryResponse
import com.gitlab.sszuev.flashcards.api.v1.models.UploadDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UploadDictionaryResponse
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.body
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DictionaryControllerRunTest {

    @Order(1)
    @Test
    fun `test get-all-dictionaries success`() = testSecuredApp {
        val requestBody = GetAllDictionariesRequest(
            requestId = "success-request",
        )
        val response = testPost("/v1/api/dictionaries/get-all", requestBody)
        val res = response.body<GetAllDictionariesResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.dictionaries)
        Assertions.assertEquals(2, res.dictionaries!!.size)
        val weather = res.dictionaries!![1]
        Assertions.assertEquals("Weather", weather.name)
        Assertions.assertEquals("en", weather.sourceLang)
        Assertions.assertEquals("ru", weather.targetLang)
    }

    @Order(2)
    @Test
    fun `test create-dictionary success`() = testSecuredApp {
        val requestBody = CreateDictionaryRequest(
            requestId = "success-request",
            dictionary = DictionaryResource(
                name = "xxx",
                sourceLang = "sS",
                targetLang = "TT",
            ),
        )
        val response = testPost("/v1/api/dictionaries/create", requestBody)
        val res = response.body<CreateDictionaryResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.dictionary)
        Assertions.assertEquals(true, res.dictionary!!.dictionaryId?.matches("\\d+".toRegex()))
        Assertions.assertEquals("xxx", res.dictionary!!.name)
        Assertions.assertEquals("ss", res.dictionary!!.sourceLang)
        Assertions.assertEquals("tt", res.dictionary!!.targetLang)
    }

    @Order(3)
    @Test
    fun `test update-dictionary success`() = testSecuredApp {
        val requestBody = UpdateDictionaryRequest(
            requestId = "success-request",
            dictionary = DictionaryResource(
                dictionaryId = "2",
                name = "Weather-42",
                sourceLang = "EN",
                targetLang = "RU",
            ),
        )
        val response = testPost("/v1/api/dictionaries/update", requestBody)
        val res = response.body<UpdateDictionaryResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.dictionary)
        Assertions.assertEquals(true, res.dictionary!!.dictionaryId?.matches("\\d+".toRegex()))
        Assertions.assertEquals("Weather-42", res.dictionary!!.name)
        Assertions.assertEquals("en", res.dictionary!!.sourceLang)
        Assertions.assertEquals("ru", res.dictionary!!.targetLang)
    }

    @Order(4)
    @Test
    fun `test delete-dictionary success`() = testSecuredApp {
        val requestBody = DeleteDictionaryRequest(
            requestId = "success-request",
            dictionaryId = "1",
        )
        val response = testPost("/v1/api/dictionaries/delete", requestBody)
        val res = response.body<DeleteDictionaryResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
    }

    @Order(5)
    @Test
    fun `test download-dictionary success`() = testSecuredApp {
        val requestBody = DownloadDictionaryRequest(
            requestId = "success-request",
            dictionaryId = "2",
            type = "xml",
        )
        val response = testPost("/v1/api/dictionaries/download", requestBody)
        val res = response.body<DownloadDictionaryResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.resource) { "can't find resource" }
        Assertions.assertTrue(res.resource!!.size in 50_000..100_000) { "unexpected resource size: ${res.resource!!.size}" }
    }

    @Order(6)
    @Test
    fun `test upload-dictionary success`() = testSecuredApp {
        val txt = """
            <?xml version="1.0" encoding="UTF-16"?>
            <dictionary 
                formatVersion="6"  
                title="Test Dictionary"  
                userId="777" 
                sourceLanguageId="1033" 
                destinationLanguageId="1049" 
                targetNamespace="http://www.abbyy.com/TutorDictionary">
            </dictionary>
        """.trimIndent()
        val requestBody = UploadDictionaryRequest(
            requestId = "success-request",
            resource = txt.toByteArray(Charsets.UTF_16),
            type = "xml",
        )
        val response = testPost("/v1/api/dictionaries/upload", requestBody)
        val res = response.body<UploadDictionaryResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.dictionary)
        Assertions.assertTrue(res.dictionary!!.dictionaryId!!.matches("\\d+".toRegex()))
        Assertions.assertEquals("Test Dictionary", res.dictionary!!.name)
        Assertions.assertEquals("en", res.dictionary!!.sourceLang)
        Assertions.assertEquals("ru", res.dictionary!!.targetLang)
        Assertions.assertNotNull(res.dictionary!!.total)
        Assertions.assertNotNull(res.dictionary!!.learned)
        Assertions.assertEquals(
            listOf(
                "noun",
                "verb",
                "adjective",
                "adverb",
                "pronoun",
                "preposition",
                "conjunction",
                "interjection",
                "article",
                "numeral",
                "participle",
            ), res.dictionary!!.partsOfSpeech
        )
    }
}