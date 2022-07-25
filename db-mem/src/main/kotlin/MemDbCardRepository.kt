package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.notFoundDbError
import com.gitlab.sszuev.flashcards.dbmem.dao.Dictionary
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository

class MemDbCardRepository(config: MemDbConfig = MemDbConfig()) : DbCardRepository {
    private val dictionaries: Map<Long, Dictionary> =
        DictionaryStore.getDictionaries(config.dataLocation).associateBy { it.id }

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        val dictionary = dictionaries[id.asDbId()]
            ?: return CardEntitiesDbResponse(
                cards = emptyList(),
                errors = listOf(notFoundDbError(operation = "getAllCards", fieldName = id.asString()))
            )
        return CardEntitiesDbResponse(
            cards = dictionary.cards.map { it.toEntity() },
            errors = emptyList()
        )
    }
}

