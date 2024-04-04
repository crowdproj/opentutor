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
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.ImportDictionaryDbResponse

class MemDbDictionaryRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbDictionaryRepository {

    private val database = MemDatabase.get(databaseLocation = dbConfig.dataLocation)

    override fun findDictionaryById(dictionaryId: String): DbDictionary? =
        database.findDictionaryById(dictionaryId.toLong())?.toDbDictionary()

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> =
        this.database.findDictionariesByUserId(userId.toLong()).map { it.toDbDictionary() }

    override fun createDictionary(entity: DbDictionary): DbDictionary =
        database.saveDictionary(entity.toMemDbDictionary().copy(changedAt = systemNow())).toDbDictionary()

    override fun deleteDictionary(dictionaryId: String): DbDictionary {
        require(dictionaryId.isNotBlank())
        val id = dictionaryId.toLong()
        val found = database.findDictionaryById(id)?.toDbDictionary()
            ?: throw DbDataException("Can't find dictionary $id")
        if (!database.deleteDictionaryById(id)) {
            throw DbDataException("Can't delete dictionary $id")
        }
        return found
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

    @Suppress("DuplicatedCode", "SameParameterValue")
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