package com.gitlab.sszuev.flashcards.translation.impl

import com.gitlab.sszuev.flashcards.utilities.get
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TranslationSettings {
    private val logger = LoggerFactory.getLogger(TranslationSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val getResourceTimeoutMs = conf.get("translation.get-resource-timeout-ms", default = 5000L)
    val httpClientConnectTimeoutMs = conf.get("translation.http-client.connect-timeout-ms", default = 3000L)
    val httpClientRequestTimeoutMs = conf.get("translation.http-client.request-timeout-ms", default = 3000L)
    val translationServiceLingueeApiHost =
        conf.get("translation.service.linguee.api-host", default = "linguee-api.fly.dev")
    val translationServiceLingueeApiPath =
        conf.get("translation.service.linguee.api-path", default = "/api/v2/translations")

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |get-resource-timeout-ms        = $getResourceTimeoutMs
            |http-client-connect-timeout-ms = $httpClientConnectTimeoutMs
            |http-client-request-timeout-ms = $httpClientRequestTimeoutMs
            |service-linguee-host           = $translationServiceLingueeApiHost
            |service-linguee-path           = $translationServiceLingueeApiPath
            """.replaceIndentByMargin("\t")
    }

}