package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository

class MockDbDictionaryRepository(
    private val invokeFindDictionaryById: (String) -> DbDictionary? = { null },
    private val invokeFindDictionariesByIdIn: (Iterable<String>) -> Sequence<DbDictionary> = { emptySequence() },
    private val invokeGetAllDictionaries: (String) -> Sequence<DbDictionary> = { emptySequence() },
    private val invokeCreateDictionary: (DbDictionary) -> DbDictionary = { DbDictionary.NULL },
    private val invokeDeleteDictionary: (String) -> DbDictionary = { DbDictionary.NULL },
) : DbDictionaryRepository {

    override fun findDictionaryById(dictionaryId: String): DbDictionary? = invokeFindDictionaryById(dictionaryId)

    override fun findDictionariesByIdIn(dictionaryIds: Iterable<String>): Sequence<DbDictionary> =
        invokeFindDictionariesByIdIn(dictionaryIds)

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> = invokeGetAllDictionaries(userId)

    override fun createDictionary(entity: DbDictionary): DbDictionary = invokeCreateDictionary(entity)

    override fun deleteDictionary(dictionaryId: String): DbDictionary = invokeDeleteDictionary(dictionaryId)

}