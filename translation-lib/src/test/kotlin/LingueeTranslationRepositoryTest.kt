package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationExample
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LingueeTranslationRepositoryTest {

    companion object {

        private const val HOST = "linguee-api.fly.dev"

        fun createMockEngine(
            sourceLang: String,
            targetLang: String,
            word: String,
        ): MockEngine {
            return MockEngine { request ->
                if (request.url.host != HOST) {
                    return@MockEngine empty()
                }
                if (request.url.parameters.isEmpty()) {
                    return@MockEngine empty()
                }
                if (sourceLang != request.url.parameters["src"]) {
                    return@MockEngine empty()
                }
                if (targetLang != request.url.parameters["dst"]) {
                    return@MockEngine empty()
                }
                if (word != request.url.parameters["query"]) {
                    return@MockEngine empty()
                }

                val responseJson = """
                    [
                      {
                        "featured": false,
                        "text": "$word",
                        "pos": "noun",
                        "forms": [],
                        "grammar_info": null,
                        "audio_links": [],
                        "translations": [
                          {
                            "featured": true,
                            "text": "translation for $word",
                            "pos": "noun",
                            "audio_links": [],
                            "examples": [
                              {
                                "src": "This is a $word example.",
                                "dst": "Это пример перевода слова $word."
                              }
                            ],
                            "usage_frequency": "often"
                          }
                        ]
                      }
                    ]
                """.trimIndent()

                return@MockEngine respond(
                    content = responseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        private fun MockRequestHandleScope.empty() = respond(ByteArray(0), HttpStatusCode.BadRequest)

        private fun createHttpClient(engine: HttpClientEngine): HttpClient {
            return HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                install(HttpTimeout)
                expectSuccess = true
            }
        }
    }

    @Test
    fun testFetchTranslation() = runBlocking {
        val testWord1 = "test-1"
        val testLang1 = "en"
        val testLang2 = "ru"
        val service1 = LingueeTranslationRepository(
            {
                createHttpClient(
                    createMockEngine(
                        sourceLang = testLang1,
                        targetLang = testLang2,
                        word = testWord1,
                    )
                )
            },
            config = TranslationConfig()
        )
        val res1 = service1.fetch(
            sourceLang = testLang1, targetLang = testLang2, word = testWord1
        )
        val expected = listOf(
            TranslationEntity(
                word = testWord1,
                partOfSpeech = "noun",
                translations = listOf(listOf("translation for $testWord1")),
                examples = listOf(
                    TranslationExample(
                        text = "This is a test-1 example.",
                        translation = "Это пример перевода слова test-1."
                    )
                )
            )
        )
        Assertions.assertEquals(expected, res1)
    }
}