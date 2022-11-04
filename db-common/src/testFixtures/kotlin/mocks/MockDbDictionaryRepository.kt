package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.*

class MockDbDictionaryRepository(
    private val invokeGetAllDictionaries: (AppUserId) -> DictionariesDbResponse = { DictionariesDbResponse.EMPTY },
    private val invokeDeleteDictionary: (DictionaryId) -> DeleteDictionaryDbResponse = { DeleteDictionaryDbResponse.EMPTY },
    private val invokeDownloadDictionary: (DictionaryId) -> DownloadDictionaryDbResponse = { DownloadDictionaryDbResponse.EMPTY },
    private val invokeUploadDictionary: (AppUserId, ResourceEntity) -> UploadDictionaryDbResponse = { _, _ -> UploadDictionaryDbResponse.EMPTY },
) : DbDictionaryRepository {

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        return invokeGetAllDictionaries(userId)
    }

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        return invokeDeleteDictionary(id)
    }

    override fun downloadDictionary(id: DictionaryId): DownloadDictionaryDbResponse {
        return invokeDownloadDictionary(id)
    }

    override fun uploadDictionary(userId: AppUserId, resource: ResourceEntity): UploadDictionaryDbResponse {
        return invokeUploadDictionary(userId, resource)
    }
}