package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.net.URL

private val logger = LoggerFactory.getLogger(LingueeTranslationRepository::class.java)

class LingueeTranslationRepository(
    private val httpClient: HttpClient = defaultHttpClient,
    private val config: TranslationConfig = TranslationConfig(),
) : TranslationRepository {

    private val apiUrl = URL(config.translationServiceLingueeApi)

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): List<TranslationEntity> {
        logger.info("::[LINGUEE][(${sourceLang} -> ${targetLang}):$word]")
        return try {
            withTimeout(config.getResourceTimeoutMs) {
                fetchResource(sourceLang, targetLang, word).map { it.toTWord() }
            }
        } catch (ex: Exception) {
            logger.error("::[LINGUEE][(${sourceLang} -> ${targetLang}):$word], error: ${ex.message}", ex)
            throw ex
        }
    }

    private suspend fun fetchResource(sourceLang: String, targetLang: String, word: String): List<LingueeEntry> {
        if (sourceLang !in SUPPORTED_LANGS) {
            throw IllegalArgumentException("Invalid source lang: '$sourceLang'")
        }
        if (targetLang !in SUPPORTED_LANGS) {
            throw IllegalArgumentException("Invalid target lang: '$targetLang'")
        }
        if (word.isBlank()) {
            throw IllegalArgumentException("Word is required")
        }
        return httpClient.get {
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            url {
                protocol = if (apiUrl.protocol == "http") URLProtocol.HTTP else URLProtocol.HTTPS
                host = apiUrl.host
                if (apiUrl.port != -1) {
                    port = apiUrl.port
                }
                encodedPath = apiUrl.path
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

    companion object {
        val SUPPORTED_LANGS = setOf(
            "bg",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "es",
            "et",
            "fi",
            "fr",
            "hu",
            "it",
            "ja",
            "lt",
            "lv",
            "mt",
            "nl",
            "pl",
            "pt",
            "ro",
            "ru",
            "sk",
            "sl",
            "sv",
            "zh"
        )
    }
}