package com.gitlab.sszuev.flashcards.model.domain

/**
 * Describes audio-resource.
 */
data class ResourceEntity(
    val resourceId: ResourceId = ResourceId.NONE,
    val data: ByteArray = ByteArray(0),
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
        val DUMMY = ResourceEntity()
    }
}