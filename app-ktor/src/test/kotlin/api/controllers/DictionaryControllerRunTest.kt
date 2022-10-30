package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.dbmem.DictionaryStore
import com.gitlab.sszuev.flashcards.dbmem.IdSequences
import com.gitlab.sszuev.flashcards.testPost
import com.gitlab.sszuev.flashcards.testSecuredApp
import io.ktor.client.call.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class DictionaryControllerRunTest {

    @AfterEach
    fun resetDb() {
        IdSequences.globalIdsGenerator.reset()
        DictionaryStore.clear()
    }

    @Test
    fun `test get-all-dictionaries success`() = testSecuredApp {
        val requestBody = GetAllDictionariesRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
        )
        val response = testPost("/v1/api/dictionaries/get-all", requestBody)
        val res = response.body<GetAllDictionariesResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Has errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.dictionaries)
        Assertions.assertEquals(1, res.dictionaries!!.size)
        val weather = res.dictionaries!![0]
        Assertions.assertEquals("Weather", weather.name)
        Assertions.assertEquals("EN", weather.sourceLang)
        Assertions.assertEquals("RU", weather.targetLang)
    }

    @Test
    fun `test delete-dictionary success`() = testSecuredApp {
        val requestBody = DeleteDictionaryRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            dictionaryId = "1",
        )
        val response = testPost("/v1/api/dictionaries/delete", requestBody)
        val res = response.body<DeleteDictionaryResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
    }

    @Test
    fun `test download-dictionary success`() = testSecuredApp {
        val requestBody = DownloadDictionaryRequest(
            requestId = "success-request",
            debug = DebugResource(mode = RunMode.TEST),
            dictionaryId = "1",
        )
        val response = testPost("/v1/api/dictionaries/download", requestBody)
        val res = response.body<DownloadDictionaryResponse>()
        Assertions.assertEquals(200, response.status.value)
        Assertions.assertEquals("success-request", res.requestId)
        Assertions.assertNull(res.errors) { "Errors: ${res.errors}" }
        Assertions.assertEquals(Result.SUCCESS, res.result)
        Assertions.assertNotNull(res.resource)
        Assertions.assertEquals(58138, res.resource!!.size)
    }
}