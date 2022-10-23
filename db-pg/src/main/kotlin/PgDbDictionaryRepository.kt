package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

class PgDbDictionaryRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
) : DbDictionaryRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        if (userId == AppUserId.NONE) {
            DictionariesDbResponse(dictionaries = emptyList())
        }
        return connection.execute {
            val dictionaries = Dictionary.find(
                Dictionaries.userId eq userId.asRecordId()
            ).map { it.toEntity() }
            DictionariesDbResponse(dictionaries = dictionaries)
        }
    }

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        return connection.execute {
            val cardIds = Cards.select {
                Cards.dictionaryId eq id.asDbId()
            }.map {
                it[Cards.id]
            }
            Examples.deleteWhere {
                this.cardId inList cardIds
            }
            Translations.deleteWhere {
                this.cardId inList cardIds
            }
            Cards.deleteWhere {
                this.id inList cardIds
            }
            val res = Dictionaries.deleteWhere {
                Dictionaries.id eq id.asDbId()
            }
            DeleteDictionaryDbResponse(
                if (res == 0) listOf(noDictionaryFoundDbError(operation = "deleteDictionary", id = id)) else emptyList()
            )
        }
    }

}