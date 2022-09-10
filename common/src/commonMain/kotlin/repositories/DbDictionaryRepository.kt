package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity

interface DbDictionaryRepository {
    fun getAllDictionaries(userId: AppUserId): DictionaryEntitiesDbResponse
}

data class DictionaryEntitiesDbResponse(
    val dictionaries: List<DictionaryEntity>,
    val errors: List<AppError> = emptyList()
) {
    companion object {
        val EMPTY = DictionaryEntitiesDbResponse(dictionaries = emptyList(), errors = emptyList())
    }
}