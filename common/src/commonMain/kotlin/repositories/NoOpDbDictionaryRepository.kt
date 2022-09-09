package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.domain.UserId

object NoOpDbDictionaryRepository : DbDictionaryRepository {
    override fun getAllDictionaries(userId: UserId): DictionaryEntitiesDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}