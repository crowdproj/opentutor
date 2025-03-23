package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(YandexTranslationRepository::class.java)

class YandexTranslationRepository(
    private val httpClient: HttpClient = defaultHttpClient,
    private val config: TranslationConfig = TranslationConfig(),
) : TranslationRepository {

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): List<TranslationEntity> {
        val src = sourceLang.trim().lowercase()
        val dst = targetLang.trim().lowercase()
        val targets = SUPPORTED_LANGUAGE_PAIRS[src] ?: run {
            logger.error("sourceLang: $src is not supported")
            return emptyList()
        }
        if (!targets.contains(dst)) {
            logger.error("targetLang: $dst is not supported")
            return emptyList()
        }
        logger.info("[YANDEX-TRANSLATION] ::: [${SUPPORTED_LANGS[sourceLang]} -> ${SUPPORTED_LANGS[targetLang]}] '$word'")
        return try {
            withTimeout(config.getResourceTimeoutMs) {
                fetchResource(word, "$src-$dst").toTWords()
            }
        } catch (ex: Exception) {
            logger.error("::[YANDEX-TRANSLATION][(${sourceLang} -> ${targetLang}):$word], error: ${ex.message}", ex)
            throw ex
        }
    }

    private suspend fun fetchResource(
        word: String,
        langPair: String,
    ): YandexEntry {
        val response: HttpResponse = httpClient.get(config.translationServiceYandexApi) {
            parameter("key", config.translationServiceYandexKey)
            parameter("lang", langPair)
            parameter("text", word)
        }
        return response.body()
    }

    companion object {
        val SUPPORTED_LANGUAGE_PAIRS = mapOf(
            "en" to setOf("ru"),
            "ru" to setOf("en", "de", "fr", "es", "it", "tr", "uk"),
            "de" to setOf("ru"),
            "fr" to setOf("ru"),
            "es" to setOf("ru"),
            "it" to setOf("ru"),
            "tr" to setOf("ru"),
            "uk" to setOf("ru")
        )

        val SUPPORTED_LANGS = mapOf(
            "de" to "German",
            "en" to "English",
            "es" to "Spanish",
            "fr" to "French",
            "ru" to "Russian",
            "uk" to "Ukrainian",
            "tr" to "Turkish",
            "it" to "Italian",
        )

    }
}