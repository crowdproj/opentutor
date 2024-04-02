package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

interface DbDictionaryRepository {
    fun findDictionaryById(dictionaryId: String): DbDictionary?

    fun findDictionariesByIdIn(dictionaryIds: Iterable<String>): Sequence<DbDictionary> =
        dictionaryIds.asSequence().mapNotNull { findDictionaryById(it) }

    fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse

    fun createDictionary(userId: AppUserId, entity: DictionaryEntity): DictionaryDbResponse

    fun removeDictionary(userId: AppUserId, dictionaryId: DictionaryId): RemoveDictionaryDbResponse

    fun importDictionary(userId: AppUserId, dictionaryId: DictionaryId): ImportDictionaryDbResponse

    fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse

}

data class DictionariesDbResponse(
    val dictionaries: List<DictionaryEntity>,
    val errors: List<AppError> = emptyList()
) {
    companion object {
        val EMPTY = DictionariesDbResponse(dictionaries = emptyList(), errors = emptyList())
    }
}

data class RemoveDictionaryDbResponse(
    val dictionary: DictionaryEntity = DictionaryEntity.EMPTY,
    val errors: List<AppError> = emptyList(),
) {
    constructor(error: AppError) : this(errors = listOf(error))

    companion object {
        val EMPTY = RemoveDictionaryDbResponse()
    }
}

data class ImportDictionaryDbResponse(
    val resource: ResourceEntity = ResourceEntity.DUMMY,
    val errors: List<AppError> = emptyList()
) {
    constructor(error: AppError) : this(errors = listOf(error))

    companion object {
        val EMPTY = ImportDictionaryDbResponse()
    }
}

data class DictionaryDbResponse(
    val dictionary: DictionaryEntity = DictionaryEntity.EMPTY,
    val errors: List<AppError> = emptyList()
) {
    constructor(error: AppError) : this(errors = listOf(error))

    companion object {
        val EMPTY = DictionaryDbResponse()
    }
}