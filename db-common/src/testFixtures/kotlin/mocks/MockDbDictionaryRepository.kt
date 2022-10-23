package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse

class MockDbDictionaryRepository(
    private val invokeGetAllDictionaries: (AppUserId) -> DictionariesDbResponse = { DictionariesDbResponse.EMPTY },
    private val invokeDeleteDictionary: (DictionaryId) -> DeleteDictionaryDbResponse = { DeleteDictionaryDbResponse.EMPTY },
) : DbDictionaryRepository {

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        return invokeGetAllDictionaries(userId)
    }

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        return invokeDeleteDictionary(id)
    }
}