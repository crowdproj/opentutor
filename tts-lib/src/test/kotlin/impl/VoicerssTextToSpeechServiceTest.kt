package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TTSConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class VoicerssTextToSpeechServiceTest {

    companion object {
        fun createMockEngine(
            res: ByteArray,
            lang: String,
            format: String,
            word: String,
            key: String,
        ): MockEngine {
            return MockEngine { request ->
                if (request.url.host != "api.voicerss.org") {
                    return@MockEngine empty()
                }
                if (request.url.parameters.isEmpty()) {
                    return@MockEngine empty()
                }
                if (lang != request.url.parameters["hl"]) {
                    return@MockEngine empty()
                }
                if (format != request.url.parameters["f"]) {
                    return@MockEngine empty()
                }
                if (key != request.url.parameters["key"]) {
                    return@MockEngine empty()
                }
                if (word != request.url.parameters["src"]) {
                    return@MockEngine empty()
                }
                return@MockEngine respond(
                    content = ByteReadChannel(res),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/octet-stream")
                )
            }
        }

        private fun MockRequestHandleScope.empty() = respond(ByteArray(0), HttpStatusCode.BadRequest)

        private fun createHttpClient(engine: HttpClientEngine): HttpClient {
            return HttpClient(engine) {
                install(HttpTimeout)
            }
        }
    }

    @Test
    fun testGetResource() = runBlocking {
        val testData = ByteArray(424) { 42 }
        val testWord1 = "test-1"
        val testLang1 = "XX"
        val service1 = VoicerssTextToSpeechService(
            {
                createHttpClient(
                    createMockEngine(
                        res = testData,
                        lang = testLang1,
                        format = "f1",
                        key = "s1",
                        word = testWord1,
                    )
                )
            },
            config = TTSConfig().copy(ttsServiceVoicerssFormat = "f1", ttsServiceVoicerssKey = "s1")
        )
        val res1 = service1.getResource("$testLang1:$testWord1")
        Assertions.assertNull(res1)

        val service2 = VoicerssTextToSpeechService({
            createHttpClient(
                createMockEngine(
                    res = testData,
                    lang = "en-us",
                    format = "16khz_16bit_stereo",
                    key = "secret",
                    word = "test-2",
                )
            )
        })
        val res2 = service2.getResource("EN:test-2")
        Assertions.assertArrayEquals(testData, res2)
    }
}