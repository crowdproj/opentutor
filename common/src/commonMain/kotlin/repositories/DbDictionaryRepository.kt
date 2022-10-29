package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

interface DbDictionaryRepository {
    fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse

    fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse

    fun downloadDictionary(id: DictionaryId): DictionaryResourceDbResponse
}

data class DictionariesDbResponse(
    val dictionaries: List<DictionaryEntity>,
    val errors: List<AppError> = emptyList()
) {
    companion object {
        val EMPTY = DictionariesDbResponse(dictionaries = emptyList(), errors = emptyList())
    }
}

data class DeleteDictionaryDbResponse(val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = DeleteDictionaryDbResponse(errors = emptyList())
    }
}

data class DictionaryResourceDbResponse(val resource: ResourceEntity, val errors: List<AppError> = emptyList()) {
    companion object {
        val EMPTY = DictionaryResourceDbResponse(resource = ResourceEntity.DUMMY)
    }
}