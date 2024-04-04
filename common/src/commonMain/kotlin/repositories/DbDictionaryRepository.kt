package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

interface DbDictionaryRepository {
    /**
     * Finds dictionary by id.
     */
    fun findDictionaryById(dictionaryId: String): DbDictionary?

    /**
     * Finds dictionaries by their id.
     */
    fun findDictionariesByIdIn(dictionaryIds: Iterable<String>): Sequence<DbDictionary> =
        dictionaryIds.asSequence().mapNotNull { findDictionaryById(it) }

    /**
     * Finds dictionaries by user id.
     */
    fun findDictionariesByUserId(userId: String): Sequence<DbDictionary>

    /**
     * Creates dictionary.
     * @throws IllegalArgumentException if the specified dictionary has illegal structure
     */
    fun createDictionary(entity: DbDictionary): DbDictionary

    /**
     * Deletes dictionary by id.
     * @throws IllegalArgumentException wrong [dictionaryId]
     * @throws DbDataException dictionary not found.
     */
    fun deleteDictionary(dictionaryId: String): DbDictionary

    fun importDictionary(userId: AppUserId, dictionaryId: DictionaryId): ImportDictionaryDbResponse

    fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse

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