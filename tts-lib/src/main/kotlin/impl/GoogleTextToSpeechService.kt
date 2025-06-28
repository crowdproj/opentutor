package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TTSConfig
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.toResourcePath
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.texttospeech.v1.AudioConfig
import com.google.cloud.texttospeech.v1.AudioEncoding
import com.google.cloud.texttospeech.v1.SynthesisInput
import com.google.cloud.texttospeech.v1.TextToSpeechClient
import com.google.cloud.texttospeech.v1.TextToSpeechSettings
import com.google.cloud.texttospeech.v1.VoiceSelectionParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.threeten.bp.Duration

private val logger = LoggerFactory.getLogger(GoogleTextToSpeechService::class.java)

class GoogleTextToSpeechService(
    private val resourceIdMapper: (String) -> Pair<String, String>? = { toResourcePath(it) },
    config: TTSConfig = TTSConfig(),
) : TextToSpeechService {

    private val timeoutMs = config.getResourceTimeoutMs

    private val credentials = GoogleTextToSpeechService::class.java.getResourceAsStream("/google-key.json")
        ?: throw IllegalStateException("Unable to obtain google key json")

    private val settings = TextToSpeechSettings.newBuilder()
        .setCredentialsProvider { ServiceAccountCredentials.fromStream(credentials) }
        .apply {
            this.synthesizeSpeechSettings().retrySettings =
                this.synthesizeSpeechSettings()
                    .retrySettings
                    .toBuilder()
                    .setTotalTimeout(Duration.ofMillis(timeoutMs))
                    .setInitialRpcTimeout(Duration.ofMillis(timeoutMs))
                    .setMaxRpcTimeout(Duration.ofMillis(timeoutMs))
                    .setMaxAttempts(1)
                    .build()
        }
        .build()

    private val client = TextToSpeechClient.create(settings).also {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                logger.info("::[GOOGLE-TTS] Close on shutdown")
                it.close()
            } catch (ex: Exception) {
                logger.warn("::[GOOGLE-TTS] Failed to close TextToSpeechClient", ex)
            }
        })
    }

    override suspend fun getResource(id: String, vararg args: String): ByteArray? = withContext(Dispatchers.IO) {
        logger.debug("::[GOOGLE-TTS] id=$id")
        val langToWord = resourceIdMapper(id) ?: run {
            logger.error("::[GOOGLE-TTS] wrong id: $id")
            return@withContext null
        }
        val lang = languagesJavaTagToGoogleTag[langToWord.first]?.get(0) ?: run {
            logger.error("::[GOOGLE-TTS] can't determine google language for ${langToWord.first}")
            return@withContext null
        }
        val word = langToWord.second
        logger.info("::[GOOGLE-TTS] $lang [${languagesGoogleTagToName[lang]}] ::: '$word'")

        try {
            val input = SynthesisInput.newBuilder().setText(word).build()

            val voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(lang)
                .build()

            val audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build()

            val response = client.synthesizeSpeech(input, voice, audioConfig)
            val audioBytes = response.audioContent
            audioBytes.toByteArray()
        } catch (ex: Exception) {
            logger.error("::[GOOGLE-TTS] Can't get resource for [${lang}:$word]")
            throw ex
        }
    }

    companion object {
        private val languagesGoogleTagToName = mapOf(
            "af-ZA" to "Afrikaans (South Africa)",
            "am-ET" to "Amharic (Ethiopia)",
            "ar-XA" to "ar-XA",
            "bg-BG" to "Bulgarian (Bulgaria)",
            "bn-IN" to "Bangla (India)",
            "ca-ES" to "Catalan (Spain)",
            "cmn-CN" to "Chinese (Simplified, China)",
            "cmn-TW" to "Chinese (Traditional, Taiwan)",
            "cs-CZ" to "Czech (Czechia)",
            "da-DK" to "Danish (Denmark)",
            "de-DE" to "German (Germany)",
            "el-GR" to "Greek (Greece)",
            "en-AU" to "English (Australia)",
            "en-GB" to "English (United Kingdom)",
            "en-IN" to "English (India)",
            "en-US" to "English (United States)",
            "es-ES" to "Spanish (Spain)",
            "es-US" to "Spanish (United States)",
            "et-EE" to "Estonian (Estonia)",
            "eu-ES" to "Basque (Spain)",
            "fi-FI" to "Finnish (Finland)",
            "fil-PH" to "Filipino (Philippines)",
            "fr-CA" to "French (Canada)",
            "fr-FR" to "French (France)",
            "gl-ES" to "Galician (Spain)",
            "gu-IN" to "Gujarati (India)",
            "he-IL" to "Hebrew (Israel)",
            "hi-IN" to "Hindi (India)",
            "hu-HU" to "Hungarian (Hungary)",
            "id-ID" to "Indonesian (Indonesia)",
            "is-IS" to "Icelandic (Iceland)",
            "it-IT" to "Italian (Italy)",
            "ja-JP" to "Japanese (Japan)",
            "kn-IN" to "Kannada (India)",
            "ko-KR" to "Korean (South Korea)",
            "lt-LT" to "Lithuanian (Lithuania)",
            "lv-LV" to "Latvian (Latvia)",
            "ml-IN" to "Malayalam (India)",
            "mr-IN" to "Marathi (India)",
            "ms-MY" to "Malay (Malaysia)",
            "nb-NO" to "Norwegian Bokmål (Norway)",
            "nl-BE" to "Dutch (Belgium)",
            "nl-NL" to "Dutch (Netherlands)",
            "pa-IN" to "Punjabi (Gurmukhi, India)",
            "pl-PL" to "Polish (Poland)",
            "pt-BR" to "Portuguese (Brazil)",
            "pt-PT" to "Portuguese (Portugal)",
            "ro-RO" to "Romanian (Romania)",
            "ru-RU" to "Russian (Russia)",
            "sk-SK" to "Slovak (Slovakia)",
            "sr-RS" to "Serbian (Cyrillic, Serbia)",
            "sv-SE" to "Swedish (Sweden)",
            "ta-IN" to "Tamil (India)",
            "te-IN" to "Telugu (India)",
            "th-TH" to "Thai (Thailand)",
            "tr-TR" to "Turkish (Türkiye)",
            "uk-UA" to "Ukrainian (Ukraine)",
            "ur-IN" to "Urdu (India)",
            "vi-VN" to "Vietnamese (Vietnam)",
            "yue-HK" to "Cantonese (Traditional, Hong Kong Sar China)"
        )

        val languagesJavaTagToGoogleTag = mapOf(
            "af" to listOf("af-ZA"),
            "am" to listOf("am-ET"),
            "bg" to listOf("bg-BG"),
            "bn" to listOf("bn-IN"),
            "ca" to listOf("ca-ES"),
            "cs" to listOf("cs-CZ"),
            "da" to listOf("da-DK"),
            "de" to listOf("de-DE"),
            "el" to listOf("el-GR"),
            "en" to listOf("en-US", "en-GB", "en-IN", "en-AU"),
            "es" to listOf("es-ES", "es-US"),
            "et" to listOf("et-EE"),
            "eu" to listOf("eu-ES"),
            "fi" to listOf("fi-FI"),
            "fil" to listOf("fil-PH"),
            "fr" to listOf("fr-FR", "fr-CA"),
            "gl" to listOf("gl-ES"),
            "gu" to listOf("gu-IN"),
            "he" to listOf("he-IL"),
            "hi" to listOf("hi-IN"),
            "hu" to listOf("hu-HU"),
            "id" to listOf("id-ID"),
            "is" to listOf("is-IS"),
            "it" to listOf("it-IT"),
            "ja" to listOf("ja-JP"),
            "kn" to listOf("kn-IN"),
            "ko" to listOf("ko-KR"),
            "lt" to listOf("lt-LT"),
            "lv" to listOf("lv-LV"),
            "ml" to listOf("ml-IN"),
            "mr" to listOf("mr-IN"),
            "ms" to listOf("ms-MY"),
            "nb" to listOf("nb-NO"),
            "nl" to listOf("nl-NL", "nl-BE"),
            "pa" to listOf("pa-IN"),
            "pl" to listOf("pl-PL"),
            "pt" to listOf("pt-BR", "pt-PT"),
            "ro" to listOf("ro-RO"),
            "ru" to listOf("ru-RU"),
            "sk" to listOf("sk-SK"),
            "sr" to listOf("sr-RS"),
            "sv" to listOf("sv-SE"),
            "ta" to listOf("ta-IN"),
            "te" to listOf("te-IN"),
            "th" to listOf("th-TH"),
            "tr" to listOf("tr-TR"),
            "uk" to listOf("uk-UA"),
            "ur" to listOf("ur-IN"),
            "vi" to listOf("vi-VN"),
            "yue" to listOf("yue-HK"),
            "zh" to listOf("cmn-CN", "cmn-TW"),
        )
    }
}