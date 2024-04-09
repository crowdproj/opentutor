package com.gitlab.sszuev.flashcards.speaker

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TTSSettings {
    private val logger = LoggerFactory.getLogger(TTSSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val localDataDirectory = conf.get("tts.local.data-directory", default = "classpath:/data")
    val getResourceTimeoutMs = conf.get("tts.get-resource-timeout-ms", default = 5000L)
    val httpClientConnectTimeoutMs = conf.get("tts.http-client.connect-timeout-ms", default = 3000L)
    val httpClientRequestTimeoutMs = conf.get("tts.http-client.request-timeout-ms", default = 3000L)
    val ttsServiceVoicerssApi = conf.get("tts.service.voicerss.api", "api.voicerss.org")
    val ttsServiceVoicerssKey = conf.get("tts.service.voicerss.key", "secret")
    val ttsServiceVoicerssFormat = conf.get("tts.service.voicerss.format", "8khz_8bit_mono")
    val ttsServiceVoicerssCodec = conf.get("tts.service.voicerss.codec", "wav")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |local-data-directory           = $localDataDirectory
            |get-resource-timeout-ms        = $getResourceTimeoutMs
            |http-client-connect-timeout-ms = $httpClientConnectTimeoutMs
            |http-client-request-timeout-ms = $httpClientRequestTimeoutMs
            |service-voicerss-api           = $ttsServiceVoicerssApi
            |service-voicerss-key           = ${secret()}
            |service-voicerss-format        = $ttsServiceVoicerssFormat
            |service-voicerss-codec         = $ttsServiceVoicerssCodec
            """.replaceIndentByMargin("\t")
    }

    private fun secret(): String {
        return if (ttsServiceVoicerssKey.isBlank() || ttsServiceVoicerssKey == "secret") "no-secret" else "***"
    }
}