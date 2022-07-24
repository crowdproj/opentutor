package com.gitlab.sszuev.flashcards

import com.gitlab.sszuev.flashcards.repositories.CardDbRepository
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import io.ktor.server.config.*
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createInstance

private val logger = LoggerFactory.getLogger(AppConfig::class.java)

/**
 * See `application.conf`
 */
class AppConfig(private val config: ApplicationConfig) {
    val ttsClientRepositoryImpl: TTSResourceRepository by lazy {
        loadJavaClass("ktor.application.tts-client.impl")
    }
    val cardRepositoryImpl: CardDbRepository by lazy {
        loadJavaClass("ktor.application.card-repository.impl")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <X> loadJavaClass(implKey: String): X {
        val implName = config.propertyOrNull(implKey)?.getString()
            ?: throw ExceptionInInitializerError("Can't find $implKey.")
        logger.debug("Load class $implName.")
        val clazz = Class.forName(implName).kotlin
        val res = clazz.createInstance()
        return res as X
    }
}