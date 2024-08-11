package com.gitlab.sszuev.flashcards.repositories

object NoOpDbDictionaryRepository : DbDictionaryRepository {
    override fun findDictionaryById(dictionaryId: String): DbDictionary = noOp()

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> = noOp()

    override fun createDictionary(entity: DbDictionary): DbDictionary = noOp()

    override fun updateDictionary(entity: DbDictionary): DbDictionary = noOp()

    override fun deleteDictionary(dictionaryId: String): DbDictionary = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}