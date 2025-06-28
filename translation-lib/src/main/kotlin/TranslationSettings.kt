package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TranslationSettings {
    private val logger = LoggerFactory.getLogger(TranslationSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val httpClientConnectTimeoutMs = conf.get("translation.http-client.connect-timeout-ms", default = 2000L)
    val httpClientRequestTimeoutMs = conf.get("translation.http-client.request-timeout-ms", default = 2000L)
    val translationServiceYandexApi =
        conf.get(
            "translation.service.yandex-api",
            default = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup"
        )
    val translationServiceYandexKey = conf.get("translation.service.yandex-key", default = "secret")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |http-client-connect-timeout-ms = $httpClientConnectTimeoutMs
            |http-client-request-timeout-ms = $httpClientRequestTimeoutMs
            |translation-service            = ${whichService()}
            """.replaceIndentByMargin("\t")
    }

    private fun whichService(): String {
        val hasGoogleTranslationService = TranslationSettings::class.java.getResource("/google-key.json") != null
        val hasYandexTranslationService =
            translationServiceYandexKey.takeIf { it != "secret" }?.isNotBlank() == true
        return if (hasGoogleTranslationService && hasYandexTranslationService) {
            "YANDEX+GOOGLE"
        } else if (hasYandexTranslationService) {
            "YANDEX"
        } else if (hasGoogleTranslationService) {
            "GOOGLE"
        } else {
            "NULL"
        }
    }

}