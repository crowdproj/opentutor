package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.ImportDictionaryDbResponse

class MockDbDictionaryRepository(
    private val invokeFindDictionaryById: (String) -> DbDictionary? = { null },
    private val invokeFindDictionariesByIdIn: (Iterable<String>) -> Sequence<DbDictionary> = { emptySequence() },
    private val invokeGetAllDictionaries: (String) -> Sequence<DbDictionary> = { emptySequence() },
    private val invokeCreateDictionary: (DbDictionary) -> DbDictionary = { DbDictionary.NULL },
    private val invokeDeleteDictionary: (String) -> DbDictionary = { DbDictionary.NULL },
    private val invokeDownloadDictionary: (AppUserId, DictionaryId) -> ImportDictionaryDbResponse = { _, _ -> ImportDictionaryDbResponse.EMPTY },
    private val invokeUploadDictionary: (AppUserId, ResourceEntity) -> DictionaryDbResponse = { _, _ -> DictionaryDbResponse.EMPTY },
) : DbDictionaryRepository {

    override fun findDictionaryById(dictionaryId: String): DbDictionary? = invokeFindDictionaryById(dictionaryId)

    override fun findDictionariesByIdIn(dictionaryIds: Iterable<String>): Sequence<DbDictionary> =
        invokeFindDictionariesByIdIn(dictionaryIds)

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> = invokeGetAllDictionaries(userId)

    override fun createDictionary(entity: DbDictionary): DbDictionary = invokeCreateDictionary(entity)

    override fun deleteDictionary(dictionaryId: String): DbDictionary = invokeDeleteDictionary(dictionaryId)

    override fun importDictionary(userId: AppUserId, dictionaryId: DictionaryId): ImportDictionaryDbResponse =
        invokeDownloadDictionary(userId, dictionaryId)

    override fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse =
        invokeUploadDictionary(userId, resource)

}