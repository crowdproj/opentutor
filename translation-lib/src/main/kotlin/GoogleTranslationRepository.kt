package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.translation.api.TranslationEntity
import com.gitlab.sszuev.flashcards.translation.api.TranslationRepository
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.http.HttpTransportOptions
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.TranslateOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.Locale

private val logger = LoggerFactory.getLogger(GoogleTranslationRepository::class.java)

class GoogleTranslationRepository(
    config: TranslationConfig = TranslationConfig(),
) : TranslationRepository {

    private val credentials = GoogleTranslationRepository::class.java.getResourceAsStream("/google-key.json")
        ?: throw IllegalStateException("Unable to obtain google key json")

    private val httpTransportOptions: HttpTransportOptions = HttpTransportOptions.newBuilder()
        .setConnectTimeout(config.httpClientConnectTimeoutMs.toInt())
        .setReadTimeout(config.httpClientRequestTimeoutMs.toInt())
        .build()

    private val translate = TranslateOptions.newBuilder()
        .setCredentials(ServiceAccountCredentials.fromStream(credentials))
        .setTransportOptions(httpTransportOptions)
        .build()
        .service

    override suspend fun fetch(sourceLang: String, targetLang: String, word: String): List<TranslationEntity> =
        withContext(Dispatchers.IO) {
            require(word.isNotBlank())
            require(sourceLang.isNotBlank())
            require(targetLang.isNotBlank())
            val src = sourceLang.trim().lowercase(Locale.ROOT)
            val dst = targetLang.trim().lowercase(Locale.ROOT)
            if (src !in SUPPORTED_LANGS.keys) {
                logger.error("sourceLang: $src is not supported")
                return@withContext emptyList()
            }
            if (dst !in SUPPORTED_LANGS.keys) {
                logger.error("targetLang: $dst is not supported")
                return@withContext emptyList()
            }
            logger.info("[GOOGLE-TRANSLATION] ::: [${SUPPORTED_LANGS[src]} -> ${SUPPORTED_LANGS[dst]}] '$word'")

            try {
                val result = translate.translate(
                    word,
                    TranslateOption.sourceLanguage(src),
                    TranslateOption.targetLanguage(dst),
                    TranslateOption.format("text")
                )
                listOf(TranslationEntity(word = word, translations = listOf(listOf(result.translatedText))))
            } catch (ex: Exception) {
                logger.error("[GOOGLE-TRANSLATION][(${src} -> ${dst}):$word], error: ${ex.message}", ex)
                throw ex
            }
        }

    companion object {
        val SUPPORTED_LANGS = mapOf(
            "af" to "Afrikaans",
            "am" to "Amharic",
            "ar" to "Arabic",
            "az" to "Azerbaijani",
            "be" to "Belarusian",
            "bg" to "Bulgarian",
            "bn" to "Bangla",
            "bs" to "Bosnian",
            "ca" to "Catalan",
            "ceb" to "Cebuano",
            "co" to "Corsican",
            "cs" to "Czech",
            "cy" to "Welsh",
            "da" to "Danish",
            "de" to "German",
            "el" to "Greek",
            "en" to "English",
            "eo" to "Esperanto",
            "es" to "Spanish",
            "et" to "Estonian",
            "eu" to "Basque",
            "fa" to "Persian",
            "fi" to "Finnish",
            "fr" to "French",
            "fy" to "Western Frisian",
            "ga" to "Irish",
            "gd" to "Scottish Gaelic",
            "gl" to "Galician",
            "gu" to "Gujarati",
            "ha" to "Hausa",
            "haw" to "Hawaiian",
            "he" to "Hebrew",
            "hi" to "Hindi",
            "hmn" to "Hmong",
            "hr" to "Croatian",
            "ht" to "ht",
            "hu" to "Hungarian",
            "hy" to "Armenian",
            "id" to "Indonesian",
            "ig" to "Igbo",
            "is" to "Icelandic",
            "it" to "Italian",
            "ja" to "Japanese",
            "jv" to "Javanese",
            "ka" to "Georgian",
            "kk" to "Kazakh",
            "km" to "Khmer",
            "kn" to "Kannada",
            "ko" to "Korean",
            "ku" to "Kurdish",
            "ky" to "Kyrgyz",
            "la" to "Latin",
            "lb" to "Luxembourgish",
            "lo" to "Lao",
            "lt" to "Lithuanian",
            "lv" to "Latvian",
            "mg" to "Malagasy",
            "mi" to "Māori",
            "mk" to "Macedonian",
            "ml" to "Malayalam",
            "mn" to "Mongolian",
            "mr" to "Marathi",
            "ms" to "Malay",
            "mt" to "Maltese",
            "my" to "Burmese",
            "ne" to "Nepali",
            "nl" to "Dutch",
            "no" to "Norwegian",
            "ny" to "Nyanja",
            "or" to "Odia",
            "pa" to "Punjabi",
            "pl" to "Polish",
            "ps" to "Pashto",
            "pt" to "Portuguese",
            "qu" to "Quechua",
            "ro" to "Romanian",
            "ru" to "Russian",
            "rw" to "Kinyarwanda",
            "sd" to "Sindhi",
            "si" to "Sinhala",
            "sk" to "Slovak",
            "sl" to "Slovenian",
            "sm" to "Samoan",
            "sn" to "Shona",
            "so" to "Somali",
            "sq" to "Albanian",
            "sr" to "Serbian",
            "st" to "Southern Sotho",
            "su" to "Sundanese",
            "sv" to "Swedish",
            "sw" to "Swahili",
            "ta" to "Tamil",
            "te" to "Telugu",
            "tg" to "Tajik",
            "th" to "Thai",
            "tk" to "Turkmen",
            "tl" to "Filipino (Philippines)",
            "tr" to "Turkish",
            "tt" to "Tatar",
            "ug" to "Uyghur",
            "uk" to "Ukrainian",
            "ur" to "Urdu",
            "uz" to "Uzbek",
            "vi" to "Vietnamese",
            "xh" to "Xhosa",
            "yi" to "Yiddish",
            "yo" to "Yoruba",
            "zh" to "Chinese",
            "zu" to "Zulu"
        )
    }
}