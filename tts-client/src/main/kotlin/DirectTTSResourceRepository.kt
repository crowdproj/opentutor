package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceGet
import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.repositories.ResourceEntityTTSResponse
import com.gitlab.sszuev.flashcards.repositories.ResourceIdTTSResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.impl.LocalTextToSpeechService

class DirectTTSResourceRepository(private val service: TextToSpeechService) : TTSResourceRepository {

    override suspend fun findResourceId(filter: ResourceGet): ResourceIdTTSResponse {
        val path = filter.toPath()
        val id = if (service.containsResource(path)) {
            ResourceId(path)
        } else {
            ResourceId.NONE
        }
        return ResourceIdTTSResponse(id)
    }

    override suspend fun getResource(id: ResourceId): ResourceEntityTTSResponse {
        val data = service.getResource(id.asString())
        val res = if (data != null) {
            ResourceEntity(resourceId = id, data = data)
        } else {
            ResourceEntity.DUMMY
        }
        return ResourceEntityTTSResponse(res)
    }

    private fun ResourceGet.toPath(): String {
        return "${lang.asString()}:${word}"
    }
}

fun createLocalTTSResourceRepository(location: String = ServerSettings.localDataDirectory) =
    DirectTTSResourceRepository(LocalTextToSpeechService.load(location))