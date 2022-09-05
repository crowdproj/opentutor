package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceGet
import com.gitlab.sszuev.flashcards.model.domain.ResourceId

/**
 * Generic (TextToSpeech) interface to provide access to [ResourceEntity]s.
 * Implementations must have no-opt constructor to allow dynamic loading.
 */
interface TTSResourceRepository {

    /**
     * Returns a resource identifier that corresponds to the given [filter].
     * @param [filter][ResourceGet]
     * @return [ResourceIdTTSResponse]
     */
    suspend fun findResourceId(filter: ResourceGet): ResourceIdTTSResponse

    /**
     * Gets resource by its id.
     * @param [id][ResourceId]
     * @return [ResourceEntityTTSResponse]
     */
    suspend fun getResource(id: ResourceId): ResourceEntityTTSResponse
}

data class ResourceEntityTTSResponse(val resource: ResourceEntity, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = ResourceEntityTTSResponse(resource = ResourceEntity.DUMMY)
    }
}

data class ResourceIdTTSResponse(val id: ResourceId, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = ResourceIdTTSResponse(id = ResourceId.NONE)
    }
}