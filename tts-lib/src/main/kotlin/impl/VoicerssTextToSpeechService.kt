package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TTSConfig
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.toResourcePath
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(VoicerssTextToSpeechService::class.java)

class VoicerssTextToSpeechService(
    private val httpClient: HttpClient = defaultHttpClient,
    private val resourceIdMapper: (String) -> Pair<String, String>? = { toResourcePath(it) },
    private val config: TTSConfig = TTSConfig(),
) : TextToSpeechService {

    companion object {
        private val defaultLanguages = voicerssLanguages.keys.sorted().associateBy { it.substringBefore("-") }

        private fun languageByTag(tag: String): String? {
            val key = normalize(tag)
            return voicerssLanguages[key] ?: defaultLanguages[key]
        }

        private fun normalize(tag: String): String {
            return tag.trim().lowercase()
        }
    }

    override suspend fun getResource(id: String, vararg args: String): ByteArray? = withContext(Dispatchers.IO) {
        val langToWord = resourceIdMapper(id) ?: return@withContext null
        val lang = languageByTag(langToWord.first) ?: return@withContext null
        val word = langToWord.second
        logger.info("::[VOICERSS]$lang:::'$word'")
        try {
            val res = readBytes(lang, word)
            if (logger.isDebugEnabled) {
                logger.debug("Received data size: {}", res.size)
            }
            if (res.size < 200) {
                // Possible error: "ERROR: The subscription is expired or requests count limitation is exceeded!"
                logger.error("The data array is too small (size=${res.size}): '${res.toString(Charsets.UTF_8)}'")
                null
            } else {
                res
            }
        } catch (ex: Exception) {
            logger.error("::[VOICERSS] Can't get resource for [${lang}:$word]")
            throw ex
        }
    }

    override suspend fun containsResource(id: String): Boolean {
        val langToWord = resourceIdMapper(id) ?: return false
        return languageByTag(langToWord.first) != null
    }

    private suspend fun readBytes(lang: String, word: String): ByteArray {
        return httpClient.get {
            headers {
                append(HttpHeaders.Accept, "audio/wav")
            }
            url {
                protocol = URLProtocol.HTTP
                host = config.ttsServiceVoicerssApi
                parameter("key", config.ttsServiceVoicerssKey)
                parameter("f", config.ttsServiceVoicerssFormat)
                parameter("hl", lang)
                parameter("src", word)
                parameter("c", config.ttsServiceVoicerssCodec)
            }
            timeout {
                requestTimeoutMillis = config.httpClientRequestTimeoutMs
                connectTimeoutMillis = config.httpClientConnectTimeoutMs
            }
        }.readRawBytes()
    }
}

private val voicerssLanguages = mapOf(
    "ar-eg" to "Arabic (Egypt)",
    "ar-sa" to "Arabic (Saudi Arabia)",
    "bg-bg" to "Bulgarian",
    "ca-es" to "Catalan",
    "zh-cn" to "Chinese (China)",
    "zh-hk" to "Chinese (Hong Kong)",
    "zh-tw" to "Chinese (Taiwan)",
    "hr-hr" to "Croatian",
    "cs-cz" to "Czech",
    "da-dk" to "Danish",
    "nl-be" to "Dutch (Belgium)",
    "nl-nl" to "Dutch (Netherlands)",
    "en-gb" to "English (Great Britain)",
    "en-us" to "English (United States)",
    "en-au" to "English (Australia)",
    "en-ca" to "English (Canada)",
    "en-in" to "English (India)",
    "en-ie" to "English (Ireland)",
    "fi-fi" to "Finnish",
    "fr-fr" to "French (France)",
    "fr-ca" to "French (Canada)",
    "fr-ch" to "French (Switzerland)",
    "de-de" to "German (Germany)",
    "de-at" to "German (Austria)",
    "de-ch" to "German (Switzerland)",
    "el-gr" to "Greek",
    "he-il" to "Hebrew",
    "hi-in" to "Hindi",
    "hu-hu" to "Hungarian",
    "id-id" to "Indonesian",
    "it-it" to "Italian",
    "ja-jp" to "Japanese",
    "ko-kr" to "Korean",
    "ms-my" to "Malay",
    "nb-no" to "Norwegian",
    "pl-pl" to "Polish",
    "pt-pt" to "Portuguese (Portugal)",
    "pt-br" to "Portuguese (Brazil)",
    "ro-ro" to "Romanian",
    "ru-ru" to "Russian",
    "sk-sk" to "Slovak",
    "sl-si" to "Slovenian",
    "es-es" to "Spanish (Spain)",
    "es-mx" to "Spanish (Mexico)",
    "sv-se" to "Swedish",
    "ta-in" to "Tamil",
    "th-th" to "Thai",
    "tr-tr" to "Turkish",
    "vi-vn" to "Vietnamese"
)
