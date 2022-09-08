package com.gitlab.sszuev.flashcards.repositories

object NoOpDbDictionaryRepository : DbDictionaryRepository {
    override fun getAllDictionaries(): DictionaryEntitiesDbResponse {
        noOp()
    }

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}