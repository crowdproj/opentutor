package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.DocumentLang
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.wrongResourceDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.*
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.*
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

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        return connection.execute {
            val cardIds = Cards.select {
                Cards.dictionaryId eq id.asLong()
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
                Dictionaries.id eq id.asLong()
            }
            DeleteDictionaryDbResponse(
                if (res == 0) listOf(noDictionaryFoundDbError(operation = "deleteDictionary", id = id)) else emptyList()
            )
        }
    }

    override fun downloadDictionary(id: DictionaryId): DownloadDictionaryDbResponse {
        return connection.execute {
            val dictionary = Dictionary.findById(id.asLong())
                ?: return@execute DownloadDictionaryDbResponse(
                    resource = ResourceEntity.DUMMY,
                    listOf(noDictionaryFoundDbError(operation = "downloadDictionary", id = id))
                )
            val cards = Card.find {
                Cards.dictionaryId eq id.asLong()
            }.with(Card::examples).with(Card::translations)
            val res = dictionary.toDownloadResource(sysConfig, cards)
            val data = createWriter().write(res)
            DownloadDictionaryDbResponse(resource = ResourceEntity(id, data))
        }
    }

    override fun uploadDictionary(userId: AppUserId, resource: ResourceEntity): UploadDictionaryDbResponse {
        val document = try {
            createReader().parse(resource.data)
        } catch (ex: Exception) {
            return UploadDictionaryDbResponse(DictionaryEntity.EMPTY, listOf(wrongResourceDbError(ex)))
        }
        return connection.execute {
            val sourceLang = document.sourceLang.getOrInsert()
            val targetLang = document.targetLang.getOrInsert()
            val dictionaryId = Dictionaries.insertAndGetId {
                it[sourceLanguage] = sourceLang.id
                it[targetLanguage] = targetLang.id
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
                userId = userId,
                dictionaryId = dictionaryId.asDictionaryId(),
                name = document.name,
                sourceLang = sourceLang.toEntity(),
                targetLang = targetLang.toEntity(),
            )
            UploadDictionaryDbResponse(res)
        }
    }

    private fun DocumentLang.getOrInsert(): Language {
        val id = this.tag.asRecordId()
        val partsOfSpeech = partsOfSpeechToRecordTxt(this.partsOfSpeech)
        return Language.findById(id) ?: Language.new(this.tag) {
            this.partsOfSpeech = partsOfSpeech
        }
    }

}