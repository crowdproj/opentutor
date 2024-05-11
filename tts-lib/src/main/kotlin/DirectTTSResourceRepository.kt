package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DirectTTSResourceRepository::class.java)

class DirectTTSResourceRepository(private val service: TextToSpeechService) : TTSResourceRepository {

    override suspend fun findResource(lang: String, word: String): ByteArray? {
        val id = id(lang, word)
        if (!service.containsResource(id)) {
            if (logger.isDebugEnabled) {
                logger.debug("No resource with id = {} found", id)
            }
            return null
        }
        if (logger.isDebugEnabled) {
            logger.debug("Get resource id = {}", id)
        }
        return service.getResource(id)
    }

    companion object {
        private fun id(lang: String, word: String): String {
            return "${lang}:${word}"
        }
    }
}

fun createDirectTTSResourceRepository(): TTSResourceRepository = DirectTTSResourceRepository(createTTSService())