package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionary
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryEntitiesDbResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PgDbDictionaryRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
) : DbDictionaryRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun getAllDictionaries(userId: AppUserId): DictionaryEntitiesDbResponse {
        if (userId == AppUserId.NONE) {
            DictionaryEntitiesDbResponse(dictionaries = emptyList())
        }
        return connection.execute {
            val dictionaries = Dictionary.find(
                Dictionaries.userId eq userId.asRecordId()
            ).map { it.toEntity() }
            DictionaryEntitiesDbResponse(dictionaries = dictionaries)
        }
    }

}