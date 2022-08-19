package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import kotlin.random.Random

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbCardRepository {
    private val dictionaries = DictionaryStore.load(
        location = dbConfig.dataLocation,
        dbConfig = dbConfig,
        sysConfig = sysConfig,
    )

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

    override fun searchCard(filter: CardFilter): CardEntitiesDbResponse {
        val dictionaries = filter.dictionaryIds.mapNotNull {
            dictionaries[it.asDbId()]
        }.sortedBy { it.id }
        if (dictionaries.isEmpty()) {
            return CardEntitiesDbResponse(cards = emptyList())
        }
        var fromDb = dictionaries.flatMap { it.cards.values }.asSequence()
        if (!filter.withUnknown) {
            fromDb = fromDb.filter { sysConfig.status(it.answered) != CardStatus.LEARNED }
        }
        if (filter.random) {
            fromDb = fromDb.shuffled(Random.Default)
        }
        val cards = fromDb.take(filter.length).map { it.toEntity() }.toList()
        return CardEntitiesDbResponse(cards = cards)
    }

}

