package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.answered
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.forbiddenEntityDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.common.wrongResourceDbError
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.ImportDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.RemoveDictionaryDbResponse

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
        val timestamp = systemNow()
        val dictionary =
            database.saveDictionary(entity.toMemDbDictionary().copy(userId = userId.asLong(), changedAt = timestamp))
        return DictionaryDbResponse(dictionary = dictionary.toDictionaryEntity())
    }

    override fun removeDictionary(userId: AppUserId, dictionaryId: DictionaryId): RemoveDictionaryDbResponse {
        val timestamp = systemNow()
        val errors = mutableListOf<AppError>()
        val found = checkDictionaryUser("removeDictionary", userId, dictionaryId, errors)
        if (errors.isNotEmpty()) {
            return RemoveDictionaryDbResponse(errors = errors)
        }
        if (!database.deleteDictionaryById(dictionaryId.asLong())) {
            return RemoveDictionaryDbResponse(noDictionaryFoundDbError("removeDictionary", dictionaryId))
        }
        return RemoveDictionaryDbResponse(
            dictionary = checkNotNull(found).copy(changedAt = timestamp).toDictionaryEntity()
        )
    }

    override fun importDictionary(
        userId: AppUserId,
        dictionaryId: DictionaryId
    ): ImportDictionaryDbResponse {
        val errors = mutableListOf<AppError>()
        val found = checkDictionaryUser("importDictionary", userId, dictionaryId, errors)
        if (errors.isNotEmpty()) {
            return ImportDictionaryDbResponse(errors = errors)
        }
        checkNotNull(found)
        val cards = database.findCardsByDictionaryId(checkNotNull(found.id)).toList()
        val document = fromDatabaseToDocumentDictionary(found, cards) { sysConfig.status(it) }
        val res = try {
            createWriter().write(document)
        } catch (ex: Exception) {
            return ImportDictionaryDbResponse(wrongResourceDbError(ex))
        }
        return ImportDictionaryDbResponse(resource = ResourceEntity(resourceId = dictionaryId, data = res))
    }

    override fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse {
        val timestamp = systemNow()
        val dictionaryDocument = try {
            createReader().parse(resource.data)
        } catch (ex: Exception) {
            return DictionaryDbResponse(wrongResourceDbError(ex))
        }
        val dictionary = database.saveDictionary(
            dictionaryDocument.toMemDbDictionary().copy(userId = userId.asLong(), changedAt = timestamp)
        )
        dictionaryDocument.toMemDbCards { sysConfig.answered(it) }.forEach {
            database.saveCard(it.copy(dictionaryId = dictionary.id, changedAt = timestamp))
        }
        return DictionaryDbResponse(dictionary = dictionary.toDictionaryEntity())
    }

    @Suppress("DuplicatedCode")
    private fun checkDictionaryUser(
        operation: String,
        userId: AppUserId,
        dictionaryId: DictionaryId,
        errors: MutableList<AppError>
    ): MemDbDictionary? {
        val dictionary = database.findDictionaryById(dictionaryId.asLong())
        if (dictionary == null) {
            errors.add(noDictionaryFoundDbError(operation, dictionaryId))
            return null
        }
        if (dictionary.userId == userId.asLong()) {
            return dictionary
        }
        errors.add(forbiddenEntityDbError(operation, dictionaryId, userId))
        return null
    }
}