package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppUserId

object NoOpDbDictionaryRepository : DbDictionaryRepository {
    override fun getAllDictionaries(userId: AppUserId): DictionaryEntitiesDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}