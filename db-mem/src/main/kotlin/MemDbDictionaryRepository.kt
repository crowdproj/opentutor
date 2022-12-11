package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.answered
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.common.wrongResourceDbError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DownloadDictionaryDbResponse
import java.time.LocalDateTime

class MemDbDictionaryRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbDictionaryRepository {

    private val database = MemDatabase.get(databaseLocation = dbConfig.dataLocation)

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        val dictionaries = this.database.findDictionariesByUserId(userId.asLong())
        return DictionariesDbResponse(dictionaries = dictionaries.map { it.toDictionaryEntity() }.toList())
    }

    override fun createDictionary(userId: AppUserId, entity: DictionaryEntity): DictionaryDbResponse {
        val dictionary = database.saveDictionary(entity.toMemDbDictionary().copy(userId = userId.asLong()))
        return DictionaryDbResponse(dictionary = dictionary.toDictionaryEntity())
    }

    override fun deleteDictionary(dictionaryId: DictionaryId): DeleteDictionaryDbResponse {
        val errors = if (database.deleteDictionaryById(dictionaryId.asLong())) {
            emptyList()
        } else {
            listOf(noDictionaryFoundDbError(operation = "deleteDictionary", id = dictionaryId))
        }
        return DeleteDictionaryDbResponse(errors = errors)
    }

    override fun downloadDictionary(dictionaryId: DictionaryId): DownloadDictionaryDbResponse {
        val id = dictionaryId.asLong()
        val dictionary = database.findDictionaryById(id)
        val cards = database.findCardsByDictionaryId(id).toList()
        return if (dictionary != null) {
            val document = fromDatabaseToDocumentDictionary(dictionary, cards) { sysConfig.status(it) }
            val res = createWriter().write(document)
            DownloadDictionaryDbResponse(resource = ResourceEntity(resourceId = dictionaryId, data = res))
        } else {
            DownloadDictionaryDbResponse(
                resource = ResourceEntity.DUMMY,
                errors = listOf(noDictionaryFoundDbError(operation = "downloadDictionary", id = dictionaryId))
            )
        }
    }

    override fun uploadDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse {
        val timestamp = LocalDateTime.now()
        val dictionaryDocument = try {
            createReader().parse(resource.data)
        } catch (ex: Exception) {
            return DictionaryDbResponse(DictionaryEntity.EMPTY, listOf(wrongResourceDbError(ex)))
        }
        val dictionary = database.saveDictionary(
            dictionaryDocument.toMemDbDictionary().copy(userId = userId.asLong(), changedAt = timestamp)
        )
        dictionaryDocument.toMemDbCards { sysConfig.answered(it) }.forEach {
            database.saveCard(it.copy(dictionaryId = dictionary.id, changedAt = timestamp))
        }
        return DictionaryDbResponse(dictionary.toDictionaryEntity())
    }
}