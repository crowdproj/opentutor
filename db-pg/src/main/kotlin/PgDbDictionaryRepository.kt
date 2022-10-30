package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DownloadDictionaryDbResponse
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

class PgDbDictionaryRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
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
            ).with(Dictionary::sourceLang).with(Dictionary::targetLand).map { it.toEntity() }
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

    override fun downloadDictionary(id: DictionaryId): DownloadDictionaryDbResponse {
        return connection.execute {
            val dictionary = Dictionary.findById(id.asDbId())
                ?: return@execute DownloadDictionaryDbResponse(
                    resource = ResourceEntity.DUMMY,
                    listOf(noDictionaryFoundDbError(operation = "downloadDictionary", id = id))
                )
            val cards = Card.find {
                Cards.dictionaryId eq id.asDbId()
            }.with(Card::examples).with(Card::translations)
            val res = dictionary.toDownloadResource(sysConfig, cards)
            val data = createWriter().write(res)
            DownloadDictionaryDbResponse(resource = ResourceEntity(id, data))
        }
    }

}