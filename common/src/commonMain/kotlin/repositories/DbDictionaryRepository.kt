package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.UserId

interface DbDictionaryRepository {
    fun getAllDictionaries(userId: UserId): DictionaryEntitiesDbResponse
}

data class DictionaryEntitiesDbResponse(
    val dictionaries: List<DictionaryEntity>,
    val errors: List<AppError> = emptyList()
) {
    companion object {
        val EMPTY = DictionaryEntitiesDbResponse(dictionaries = emptyList(), errors = emptyList())
    }
}