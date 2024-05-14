package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.systemNow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class PgDbDictionaryRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
) : DbDictionaryRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    // enforce connection
    fun connect() {
        connection
    }

    override fun findDictionaryById(dictionaryId: String): DbDictionary? = connection.execute {
        PgDbDictionary.findById(dictionaryId.toLong())?.toDbDictionary()
    }

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> = connection.execute {
        PgDbDictionary.find(Dictionaries.userId eq userId).map { it.toDbDictionary() }.asSequence()
    }

    override fun createDictionary(entity: DbDictionary): DbDictionary = connection.execute {
        val timestamp = systemNow()
        val dictionaryId = Dictionaries.insertAndGetId {
            it[sourceLanguage] = entity.sourceLang.langId
            it[targetLanguage] = entity.targetLang.langId
            it[name] = entity.name
            it[userId] = entity.userId
            it[changedAt] = timestamp
        }
        entity.copy(dictionaryId = dictionaryId.value.toString())
    }

    override fun deleteDictionary(dictionaryId: String): DbDictionary = connection.execute {
        require(dictionaryId.isNotBlank())
        val id = dictionaryId.toLong()
        val found = PgDbDictionary.findById(id)?.toDbDictionary() ?: throw DbDataException("Can't find dictionary $id")
        val cardIds = Cards.selectAll().where {
            Cards.dictionaryId eq id
        }.map {
            it[Cards.id]
        }
        Cards.deleteWhere {
            this.id inList cardIds
        }
        val res = Dictionaries.deleteWhere {
            Dictionaries.id eq id
        }
        if (res != 1) {
            throw DbDataException("Can't delete dictionary $id")
        }
        found
    }

}