package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

object NoOpDbDictionaryRepository : DbDictionaryRepository {
    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        noOp()
    }

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        noOp()
    }

    override fun downloadDictionary(id: DictionaryId): DictionaryResourceDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}