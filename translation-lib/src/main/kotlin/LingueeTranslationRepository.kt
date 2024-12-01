package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
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
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(LingueeTranslationRepository::class.java)

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
    private val config: TranslationConfig = TranslationConfig(),
) : TranslationRepository {

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): List<TranslationEntity> {
        logger.info("::[LINGUEE][(${sourceLang} -> ${targetLang}):$word]")
        return try {
            withTimeout(config.getResourceTimeoutMs) {
                fetchResource(sourceLang, targetLang, word).map { it.toTWord() }
            }
        } catch (ex: Exception) {
            logger.error("::[LINGUEE] Can't get resource for [(${sourceLang} -> ${targetLang}):$word]")
            throw ex
        }
    }

    private suspend fun fetchResource(sourceLang: String, targetLang: String, word: String): List<LingueeEntry> {
        return clientProducer().use {
            it.get {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
                url {
                    protocol = URLProtocol.HTTPS
                    host = config.translationServiceLingueeApiHost
                    encodedPath = config.translationServiceLingueeApiPath
                    parameter("src", sourceLang)
                    parameter("dst", targetLang)
                    parameter("query", word)
                }
                timeout {
                    requestTimeoutMillis = config.httpClientRequestTimeoutMs
                    connectTimeoutMillis = config.httpClientConnectTimeoutMs
                }
            }.body()
        }
    }
}