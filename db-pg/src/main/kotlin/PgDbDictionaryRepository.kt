package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.wrongResourceDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.Card
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionary
import com.gitlab.sszuev.flashcards.dbpg.dao.Example
import com.gitlab.sszuev.flashcards.dbpg.dao.Examples
import com.gitlab.sszuev.flashcards.dbpg.dao.Translation
import com.gitlab.sszuev.flashcards.dbpg.dao.Translations
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DownloadDictionaryDbResponse
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
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

    override fun createDictionary(userId: AppUserId, entity: DictionaryEntity): DictionaryDbResponse {
        return connection.execute {
            val dictionaryId = Dictionaries.insertAndGetId {
                it[sourceLanguage] = entity.sourceLang.langId.asString()
                it[targetLanguage] = entity.targetLang.langId.asString()
                it[name] = entity.name
                it[Dictionaries.userId] = userId.asLong()
            }
            DictionaryDbResponse(entity.copy(dictionaryId = dictionaryId.asDictionaryId()))
        }
    }

    override fun deleteDictionary(dictionaryId: DictionaryId): DeleteDictionaryDbResponse {
        return connection.execute {
            val cardIds = Cards.select {
                Cards.dictionaryId eq dictionaryId.asLong()
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
                Dictionaries.id eq dictionaryId.asLong()
            }
            DeleteDictionaryDbResponse(
                if (res == 0) listOf(noDictionaryFoundDbError(operation = "deleteDictionary", id = dictionaryId)) else emptyList()
            )
        }
    }

    override fun downloadDictionary(dictionaryId: DictionaryId): DownloadDictionaryDbResponse {
        return connection.execute {
            val dictionary = Dictionary.findById(dictionaryId.asLong())
                ?: return@execute DownloadDictionaryDbResponse(
                    resource = ResourceEntity.DUMMY,
                    listOf(noDictionaryFoundDbError(operation = "downloadDictionary", id = dictionaryId))
                )
            val cards = Card.find {
                Cards.dictionaryId eq dictionaryId.asLong()
            }.with(Card::examples).with(Card::translations)
            val res = dictionary.toDownloadResource(sysConfig, cards)
            val data = createWriter().write(res)
            DownloadDictionaryDbResponse(resource = ResourceEntity(dictionaryId, data))
        }
    }

    override fun uploadDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse {
        val document = try {
            createReader().parse(resource.data)
        } catch (ex: Exception) {
            return DictionaryDbResponse(DictionaryEntity.EMPTY, listOf(wrongResourceDbError(ex)))
        }
        return connection.execute {
            val sourceLang = document.sourceLang
            val targetLang = document.targetLang
            val dictionaryId = Dictionaries.insertAndGetId {
                it[sourceLanguage] = sourceLang
                it[targetLanguage] = targetLang
                it[name] = document.name
                it[Dictionaries.userId] = userId.asLong()
            }
            document.cards.forEach {
                val record = Card.new {
                    copyToDbEntityRecord(dictionaryId, it, this)
                }
                it.examples.forEach {
                    Example.new {
                        this.cardId = record.id
                        this.text = it
                    }
                }
                it.translations.forEach {
                    Translation.new {
                        this.cardId = record.id
                        this.text = it
                    }
                }
            }
            val res = DictionaryEntity(
                dictionaryId = dictionaryId.asDictionaryId(),
                name = document.name,
                sourceLang = createLangEntity(sourceLang),
                targetLang = createLangEntity(targetLang),
            )
            DictionaryDbResponse(res)
        }
    }
}