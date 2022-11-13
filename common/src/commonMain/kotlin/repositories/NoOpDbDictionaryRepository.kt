package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

object NoOpDbDictionaryRepository : DbDictionaryRepository {
    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        noOp()
    }

    override fun createDictionary(userId: AppUserId, entity: DictionaryEntity): DictionaryDbResponse {
        noOp()
    }

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        noOp()
    }

    override fun downloadDictionary(id: DictionaryId): DownloadDictionaryDbResponse {
        noOp()
    }

    override fun uploadDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}