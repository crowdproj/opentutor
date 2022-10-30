package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

/**
 * Generic (TextToSpeech) interface to provide access to [ResourceEntity]s.
 * Implementations must have no-opt constructor to allow dynamic loading.
 */
interface TTSResourceRepository {

    /**
     * Returns a resource identifier that corresponds to the given [filter].
     * @param [filter][TTSResourceGet]
     * @return [TTSResourceIdResponse]
     */
    suspend fun findResourceId(filter: TTSResourceGet): TTSResourceIdResponse

    /**
     * Gets resource by its id.
     * @param [id][TTSResourceId]
     * @return [TTSResourceEntityResponse]
     */
    suspend fun getResource(id: TTSResourceId): TTSResourceEntityResponse
}

data class TTSResourceEntityResponse(val resource: ResourceEntity, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = TTSResourceEntityResponse(resource = ResourceEntity.DUMMY)
    }
}

data class TTSResourceIdResponse(val id: TTSResourceId, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = TTSResourceIdResponse(id = TTSResourceId.NONE)
    }
}