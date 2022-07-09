package com.gitlab.sszuev.flashcards.model.repositories

import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceId

/**
 * Generic (TextToSpeech) interface to provide access to [ResourceEntity]s.
 */
interface TTSResourceRepository {

    /**
     * Returns a resource identifier that corresponds to the given [word] and [lang] tag.
     * @param [word][String]
     * @param [lang][LangId]
     * @return [ResourceId] or `null` if there is no resource for the provided data
     */
    suspend fun findResourceId(word: String, lang: LangId): ResourceId?

    /**
     * Gets resource by its id.
     * @param [id][ResourceId]
     * @return [ResourceEntity]
     */
    suspend fun getResource(id: ResourceId): ResourceEntity
}