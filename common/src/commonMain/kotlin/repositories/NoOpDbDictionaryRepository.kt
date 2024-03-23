package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

object NoOpDbDictionaryRepository : DbDictionaryRepository {

    override fun findDictionaryById(dictionaryId: DictionaryId): DictionaryEntity = noOp()

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse = noOp()

    override fun createDictionary(userId: AppUserId, entity: DictionaryEntity): DictionaryDbResponse = noOp()

    override fun removeDictionary(userId: AppUserId, dictionaryId: DictionaryId): RemoveDictionaryDbResponse = noOp()

    override fun importDictionary(userId: AppUserId, dictionaryId: DictionaryId): ImportDictionaryDbResponse = noOp()

    override fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}