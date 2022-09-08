package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryEntitiesDbResponse

class PgDbDictionaryRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
): DbDictionaryRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun getAllDictionaries(): DictionaryEntitiesDbResponse {
        return connection.execute {
            val dictionaries = Dictionary.all().map { it.toEntity() }
            DictionaryEntitiesDbResponse(
                dictionaries = dictionaries,
            )
        }
    }

}