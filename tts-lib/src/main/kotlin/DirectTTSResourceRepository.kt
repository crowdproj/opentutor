package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceEntityResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceIdResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository

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

fun createDirectTTSResourceRepository(): TTSResourceRepository = DirectTTSResourceRepository(createTTSService())