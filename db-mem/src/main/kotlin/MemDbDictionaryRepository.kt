package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.systemNow

class MemDbDictionaryRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbDictionaryRepository {

    private val database = MemDatabase.get(databaseLocation = dbConfig.dataLocation)

    override fun findDictionaryById(dictionaryId: String): DbDictionary? =
        database.findDictionaryById(dictionaryId.toLong())?.toDbDictionary()

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> =
        this.database.findDictionariesByUserId(userId).map { it.toDbDictionary() }

    override fun createDictionary(entity: DbDictionary): DbDictionary =
        database.saveDictionary(entity.toMemDbDictionary().copy(changedAt = systemNow())).toDbDictionary()

    override fun deleteDictionary(dictionaryId: String): DbDictionary {
        require(dictionaryId.isNotBlank())
        val id = dictionaryId.toLong()
        val found = database.findDictionaryById(id)?.toDbDictionary()
            ?: throw DbDataException("Can't find dictionary $id")
        if (!database.deleteDictionaryById(id)) {
            throw DbDataException("Can't delete dictionary $id")
        }
        return found
    }

}