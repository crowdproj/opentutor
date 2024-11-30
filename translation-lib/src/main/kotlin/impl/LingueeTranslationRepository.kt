package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TCard
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class LingueeTranslationRepository(
    private val clientProducer: () -> HttpClient = {
        HttpClient {
            install(HttpTimeout)
            expectSuccess = true

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    },
) : TranslationRepository {

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): TCard =
        fetchResource(sourceLang, targetLang, word).toTCard()

    private suspend fun fetchResource(sourceLang: String, targetLang: String, word: String): List<LingueeEntry> {
        return clientProducer().use {
            it.get {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
                url {
                    protocol = URLProtocol.HTTPS
                    host = "linguee-api.fly.dev"
                    encodedPath = "/api/v2/translations"
                    parameter("src", sourceLang)
                    parameter("dst", targetLang)
                    parameter("query", word)
                }
                timeout {
                    requestTimeoutMillis = 3000L
                    connectTimeoutMillis = 3000L
                }
            }.body()
        }
    }
}