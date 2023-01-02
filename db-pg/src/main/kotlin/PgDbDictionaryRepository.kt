package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.common.toDocumentExamples
import com.gitlab.sszuev.flashcards.common.toDocumentTranslations
import com.gitlab.sszuev.flashcards.common.wrongResourceDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.DbPgCard
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DownloadDictionaryDbResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime

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
            val dictionaries =
                PgDbDictionary.find(Dictionaries.userId eq userId.asRecordId()).map { it.toDictionaryEntity() }
            DictionariesDbResponse(dictionaries = dictionaries)
        }
    }

    override fun createDictionary(userId: AppUserId, entity: DictionaryEntity): DictionaryDbResponse {
        return connection.execute {
            val timestamp = LocalDateTime.now()
            val dictionaryId = Dictionaries.insertAndGetId {
                it[sourceLanguage] = entity.sourceLang.langId.asString()
                it[targetLanguage] = entity.targetLang.langId.asString()
                it[name] = entity.name
                it[Dictionaries.userId] = userId.asLong()
                it[changedAt] = timestamp
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
            Cards.deleteWhere {
                this.id inList cardIds
            }
            val res = Dictionaries.deleteWhere {
                Dictionaries.id eq dictionaryId.asLong()
            }
            DeleteDictionaryDbResponse(
                if (res == 0)
                    listOf(noDictionaryFoundDbError(operation = "deleteDictionary", id = dictionaryId))
                else emptyList()
            )
        }
    }

    override fun downloadDictionary(dictionaryId: DictionaryId): DownloadDictionaryDbResponse {
        return connection.execute {
            val dictionary = PgDbDictionary.findById(dictionaryId.asLong())
                ?: return@execute DownloadDictionaryDbResponse(
                    resource = ResourceEntity.DUMMY,
                    listOf(noDictionaryFoundDbError(operation = "downloadDictionary", id = dictionaryId))
                )
            val cards = DbPgCard.find {
                Cards.dictionaryId eq dictionaryId.asLong()
            }

            val res = DocumentDictionary(
                name = dictionary.name,
                sourceLang = dictionary.sourceLang,
                targetLang = dictionary.targetLang,
                cards = cards.map { card ->
                    val word = parseCardWordsJson(card.words).firstOrNull()
                    DocumentCard(
                        text = card.text,
                        transcription = word?.transcription,
                        partOfSpeech = word?.partOfSpeech,
                        translations = word?.toDocumentTranslations() ?: emptyList(),
                        examples = word?.toDocumentExamples() ?: emptyList(),
                        status = sysConfig.status(card.answered),
                    )
                }
            )
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
            val timestamp = LocalDateTime.now()
            val sourceLang = document.sourceLang
            val targetLang = document.targetLang
            val dictionaryId = Dictionaries.insertAndGetId {
                it[sourceLanguage] = sourceLang
                it[targetLanguage] = targetLang
                it[name] = document.name
                it[Dictionaries.userId] = userId.asLong()
                it[changedAt] = timestamp
            }
            document.cards.forEach {
                DbPgCard.new {
                    this.dictionaryId = dictionaryId
                    this.text = it.text
                    this.words = it.toPgDbCardWordsJson()
                    this.details = "{}"
                    this.changedAt = timestamp
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