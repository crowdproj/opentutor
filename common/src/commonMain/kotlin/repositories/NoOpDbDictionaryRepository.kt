package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity

object NoOpDbDictionaryRepository : DbDictionaryRepository {
    override fun findDictionaryById(dictionaryId: String): DbDictionary = noOp()

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> = noOp()

    override fun createDictionary(entity: DbDictionary): DbDictionary = noOp()

    override fun removeDictionary(userId: AppUserId, dictionaryId: DictionaryId): RemoveDictionaryDbResponse = noOp()

    override fun importDictionary(userId: AppUserId, dictionaryId: DictionaryId): ImportDictionaryDbResponse = noOp()

    override fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}