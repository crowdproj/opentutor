package com.gitlab.sszuev.flashcards.model.domain

import com.gitlab.sszuev.flashcards.model.common.AppError

data class ResourceEntity(
    val cardId: ResourceId = ResourceId.NONE,
    val data: ByteArray = ByteArray(0),
    val errors: List<AppError> = listOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false
        other as ResourceEntity
        if (cardId != other.cardId) return false
        if (errors != other.errors) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var res = cardId.hashCode()
        res = 31 * res + errors.hashCode()
        res = 31 * res + data.contentHashCode()
        return res
    }
}