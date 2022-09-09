package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.model.domain.UserId
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryEntitiesDbResponse

class MemDbDictionaryRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    sysConfig: SysConfig = SysConfig(),
) : DbDictionaryRepository {
    private val dictionaries = DictionaryStore.load(
        location = dbConfig.dataLocation,
        dbConfig = dbConfig,
        sysConfig = sysConfig,
    )

    override fun getAllDictionaries(userId: UserId): DictionaryEntitiesDbResponse {
        val dictionaries = this.dictionaries.keys
            .mapNotNull { dictionaries[it] }
            .map { it.toEntity() }
            .filter { it.userId == userId }
        return DictionaryEntitiesDbResponse(dictionaries = dictionaries)
    }
}