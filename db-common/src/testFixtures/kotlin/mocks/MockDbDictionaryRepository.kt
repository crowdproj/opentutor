package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.ImportDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.RemoveDictionaryDbResponse

class MockDbDictionaryRepository(
    private val invokeFindDictionaryById: (DictionaryId) -> DictionaryEntity? = { null },
    private val invokeFindDictionariesByIdIn: (Iterable<DictionaryId>) -> Sequence<DictionaryEntity> = { emptySequence() },
    private val invokeGetAllDictionaries: (AppUserId) -> DictionariesDbResponse = { DictionariesDbResponse.EMPTY },
    private val invokeCreateDictionary: (AppUserId, DictionaryEntity) -> DictionaryDbResponse = { _, _ -> DictionaryDbResponse.EMPTY },
    private val invokeDeleteDictionary: (AppUserId, DictionaryId) -> RemoveDictionaryDbResponse = { _, _ -> RemoveDictionaryDbResponse.EMPTY },
    private val invokeDownloadDictionary: (AppUserId, DictionaryId) -> ImportDictionaryDbResponse = { _, _ -> ImportDictionaryDbResponse.EMPTY },
    private val invokeUploadDictionary: (AppUserId, ResourceEntity) -> DictionaryDbResponse = { _, _ -> DictionaryDbResponse.EMPTY },
) : DbDictionaryRepository {

    override fun findDictionaryById(dictionaryId: DictionaryId): DictionaryEntity? =
        invokeFindDictionaryById(dictionaryId)

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse = invokeGetAllDictionaries(userId)

    override fun createDictionary(userId: AppUserId, entity: DictionaryEntity): DictionaryDbResponse =
        invokeCreateDictionary(userId, entity)

    override fun removeDictionary(userId: AppUserId, dictionaryId: DictionaryId): RemoveDictionaryDbResponse =
        invokeDeleteDictionary(userId, dictionaryId)

    override fun importDictionary(userId: AppUserId, dictionaryId: DictionaryId): ImportDictionaryDbResponse =
        invokeDownloadDictionary(userId, dictionaryId)

    override fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse =
        invokeUploadDictionary(userId, resource)

    override fun findDictionariesByIdIn(dictionaryIds: Iterable<DictionaryId>): Sequence<DictionaryEntity> =
        invokeFindDictionariesByIdIn(dictionaryIds)
}