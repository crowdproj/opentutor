package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.Id
import kotlinx.serialization.Serializable

/**
 * To hold byte-array (e.g., dictionary upload/download data, audio resources).
 */
@Serializable
data class ResourceEntity(
    val resourceId: Id,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false
        other as ResourceEntity
        if (resourceId != other.resourceId) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var res = resourceId.hashCode()
        res = 31 * res + data.contentHashCode()
        return res
    }

    companion object {
        val DUMMY = ResourceEntity(resourceId = Id.NONE, data = ByteArray(0))
    }
}