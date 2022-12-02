package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceEntityResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceIdResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.impl.EspeakNgTestToSpeechService
import com.gitlab.sszuev.flashcards.speaker.impl.LocalTextToSpeechService
import com.gitlab.sszuev.flashcards.speaker.impl.VoicerssTextToSpeechService
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DirectTTSResourceRepository::class.java)

class DirectTTSResourceRepository(private val service: TextToSpeechService) : TTSResourceRepository {

    override suspend fun findResourceId(filter: TTSResourceGet): TTSResourceIdResponse {
        val path = filter.toPath()
        val id = if (service.containsResource(path)) {
            TTSResourceId(path)
        } else {
            TTSResourceId.NONE
        }
        return TTSResourceIdResponse(id)
    }

    override suspend fun getResource(id: TTSResourceId): TTSResourceEntityResponse {
        val data = service.getResource(id.asString())
        val res = if (data != null) {
            ResourceEntity(resourceId = id, data = data)
        } else {
            ResourceEntity.DUMMY
        }
        return TTSResourceEntityResponse(res)
    }

    private fun TTSResourceGet.toPath(): String {
        return "${lang.asString()}:${word}"
    }
}

fun createDirectTTSResourceRepository(): TTSResourceRepository {
    return if (TTSSettings.ttsServiceVoicerssKey.isNotBlank() && TTSSettings.ttsServiceVoicerssKey != "secret") {
        logger.info("::[TTS-SERVICE] init voicerss service")
        createVoicerssTTSResourceRepository()
    } else if (EspeakNgTestToSpeechService.isEspeakNgAvailable()) {
        logger.info("::[TTS-SERVICE] init espeak-ng service")
        createEspeakNgTTSResourceRepository()
    } else {
        logger.info("::[TTS-SERVICE] init local (test) service")
        createLocalTTSResourceRepository()
    }
}

fun createLocalTTSResourceRepository(location: String = TTSSettings.localDataDirectory) =
    DirectTTSResourceRepository(LocalTextToSpeechService.load(location))

fun createVoicerssTTSResourceRepository() = DirectTTSResourceRepository(VoicerssTextToSpeechService())

fun createEspeakNgTTSResourceRepository() = DirectTTSResourceRepository(EspeakNgTestToSpeechService())