package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteDictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionariesDbResponse

class MemDbDictionaryRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    sysConfig: SysConfig = SysConfig(),
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
}