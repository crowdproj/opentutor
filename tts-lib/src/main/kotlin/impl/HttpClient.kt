package com.gitlab.sszuev.flashcards.speaker.impl

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.speaker.impl.HttpClient")

val defaultHttpClient = HttpClient {
    install(HttpTimeout)
    expectSuccess = true
}.also {
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Close http connection on shutdown.")
        it.close()
    })
}