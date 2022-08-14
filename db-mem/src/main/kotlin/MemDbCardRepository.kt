package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.asDbId
import com.gitlab.sszuev.flashcards.common.notFoundDbError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository

class MemDbCardRepository(config: MemDbConfig = MemDbConfig()) : DbCardRepository {
    private val dictionaries = DictionaryStore.load(location = config.dataLocation, config = config)

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        val dictionary = dictionaries[id.asDbId()]
            ?: return CardEntitiesDbResponse(
                cards = emptyList(),
                errors = listOf(notFoundDbError(operation = "getAllCards", fieldName = id.asString()))
            )
        return CardEntitiesDbResponse(
            cards = dictionary.cards.values.map { it.toEntity() },
            errors = emptyList()
        )
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        val dictionaryId = card.dictionaryId.asDbId()
        val dictionary = dictionaries[dictionaryId]
            ?: return CardEntityDbResponse(
                card = CardEntity.EMPTY,
                errors = listOf(notFoundDbError(operation = "createCard", fieldName = card.dictionaryId.asString()))
            )
        val saved = card.toNewDbRecord(dictionaries.ids)
        dictionary.cards[saved.id] = saved
        dictionaries.flush(dictionaryId)
        return CardEntityDbResponse(card = saved.toEntity())
    }

}

