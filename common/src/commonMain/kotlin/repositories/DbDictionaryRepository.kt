package com.gitlab.sszuev.flashcards.repositories

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

}