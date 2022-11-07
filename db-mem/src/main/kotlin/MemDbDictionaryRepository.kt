package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.*

class MemDbDictionaryRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
    private val ids: IdSequences = IdSequences.globalIdsGenerator,
) : DbDictionaryRepository {
    private val dictionaries = DictionaryStore.load(
        dbConfig = dbConfig,
        sysConfig = sysConfig,
    )

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        val uid = userId.asLong()
        val dictionaries = this.dictionaries.keys
            .mapNotNull { dictionaries[it] }
            .filter { it.userId == uid }
            .map { it.toEntity() }
        return DictionariesDbResponse(dictionaries = dictionaries)
    }

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        val dictionary = dictionaries - id.asLong()
        val errors = if (dictionary != null) {
            emptyList()
        } else {
            listOf(noDictionaryFoundDbError(operation = "deleteDictionary", id = id))
        }
        return DeleteDictionaryDbResponse(errors = errors)
    }

    override fun downloadDictionary(id: DictionaryId): DownloadDictionaryDbResponse {
        val dictionary = dictionaries[id.asLong()]
        return if (dictionary != null) {
            val res = createWriter().write(dictionary.toDocument(false) { sysConfig.status(it) })
            DownloadDictionaryDbResponse(resource = ResourceEntity(resourceId = id, data = res))
        } else {
            DownloadDictionaryDbResponse(
                resource = ResourceEntity.DUMMY,
                errors = listOf(noDictionaryFoundDbError(operation = "downloadDictionary", id = id))
            )
        }
    }

    override fun uploadDictionary(userId: AppUserId, resource: ResourceEntity): UploadDictionaryDbResponse {
        val dictionary = try {
            createReader(ids).parse(resource.data).toDbRecord(userId = userId)
        } catch (ex: Exception) {
            return UploadDictionaryDbResponse(DictionaryEntity.EMPTY, listOf(wrongResourceDbError(ex)))
        }
        dictionaries[dictionary.id] = dictionary
        return UploadDictionaryDbResponse(dictionary.toEntity())
    }
}