package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionaryResourceDbResponse

class MemDbDictionaryRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbDictionaryRepository {
    private val dictionaries = DictionaryStore.load(
        location = dbConfig.dataLocation,
        dbConfig = dbConfig,
        sysConfig = sysConfig,
    )

    override fun getAllDictionaries(userId: AppUserId): DictionariesDbResponse {
        val dictionaries = this.dictionaries.keys
            .mapNotNull { dictionaries[it] }
            .map { it.toEntity() }
            .filter { it.userId == userId }
        return DictionariesDbResponse(dictionaries = dictionaries)
    }

    override fun deleteDictionary(id: DictionaryId): DeleteDictionaryDbResponse {
        val dictionary = dictionaries - id.asDbId()
        val errors = if (dictionary != null) {
            emptyList()
        } else {
            listOf(noDictionaryFoundDbError(operation = "deleteDictionary", id = id))
        }
        return DeleteDictionaryDbResponse(errors = errors)
    }

    override fun downloadDictionary(id: DictionaryId): DictionaryResourceDbResponse {
        val dictionary = dictionaries[id.asDbId()]
        return if (dictionary != null) {
            val res = createWriter().write(dictionary.toDocument(false) { sysConfig.status(it) })
            DictionaryResourceDbResponse(resource = ResourceEntity(resourceId = id, data = res))
        } else {
            DictionaryResourceDbResponse(
                resource = ResourceEntity.DUMMY,
                errors = listOf(noDictionaryFoundDbError(operation = "downloadDictionary", id = id))
            )
        }
    }
}