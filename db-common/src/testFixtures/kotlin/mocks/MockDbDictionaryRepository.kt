package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryEntitiesDbResponse

class MockDbDictionaryRepository(
    private val invokeGetAllDictionaries: (AppUserId) -> DictionaryEntitiesDbResponse = { DictionaryEntitiesDbResponse.EMPTY }
) : DbDictionaryRepository {

    override fun getAllDictionaries(userId: AppUserId): DictionaryEntitiesDbResponse {
        return invokeGetAllDictionaries(userId)
    }
}