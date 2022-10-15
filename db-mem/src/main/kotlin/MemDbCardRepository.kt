package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.*
import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.*
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteEntityDbResponse
import kotlin.random.Random

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
    private val ids: IdSequences = IdSequences.globalIdsGenerator,
) : DbCardRepository {
    private val dictionaries = DictionaryStore.load(
        location = dbConfig.dataLocation,
        dbConfig = dbConfig,
        sysConfig = sysConfig,
        ids = ids,
    )

    override fun getCard(id: CardId): CardEntityDbResponse {
        val card = dictionaries.keys.mapNotNull { dictionaries[it] }.mapNotNull { it.cards[id.asDbId()] }.singleOrNull()
            ?: return CardEntityDbResponse(
                card = CardEntity.EMPTY,
                errors = listOf(noCardFoundDbError(operation = "getCard", id = id))
            )
        return CardEntityDbResponse(card = card.toEntity())
    }

    override fun getAllCards(id: DictionaryId): CardEntitiesDbResponse {
        val dictionary = dictionaries[id.asDbId()]
            ?: return CardEntitiesDbResponse(
                cards = emptyList(),
                errors = listOf(noDictionaryFoundDbError(operation = "getAllCards", id = id))
            )
        val cards = dictionary.cards.values.map { it.toEntity() }
        return CardEntitiesDbResponse(
            cards = cards,
            sourceLanguage = dictionary.sourceLanguage.asLangId(),
            errors = emptyList()
        )
    }

    override fun searchCard(filter: CardFilter): CardEntitiesDbResponse {
        val dictionaries = filter.dictionaryIds.mapNotNull {
            dictionaries[it.asDbId()]
        }.sortedBy { it.id }
        if (dictionaries.isEmpty()) {
            return CardEntitiesDbResponse(cards = emptyList())
        }
        val sourceLanguages = dictionaries.map { it.sourceLanguage.asLangId() }.toSet()
        val targetLanguages = dictionaries.map { it.targetLanguage.asLangId() }.toSet()
        if (sourceLanguages.size != 1 || targetLanguages.size != 1) {
            return CardEntitiesDbResponse(
                cards = emptyList(),
                errors = listOf(wrongDictionaryLanguageFamilies(
                        operation = "searchCard",
                        dictionaryIds = filter.dictionaryIds,
                    )
                )
            )
        }
        var fromDb = dictionaries.flatMap { it.cards.values }.asSequence()
        if (!filter.withUnknown) {
            fromDb = fromDb.filter { sysConfig.status(it.answered) != CardStatus.LEARNED }
        }
        if (filter.random) {
            fromDb = fromDb.shuffled(Random.Default)
        }
        val cards = fromDb.take(filter.length).map { it.toEntity() }.toList()
        return CardEntitiesDbResponse(cards = cards, sourceLanguage = sourceLanguages.single())
    }

    override fun createCard(card: CardEntity): CardEntityDbResponse {
        requireNew(card)
        val dictionaryId = card.dictionaryId.asDbId()
        val dictionary =
            dictionaries[dictionaryId] ?: return createNoDictionaryResponseError(card.dictionaryId, "createCard")
        val record = card.toDbRecord(ids.nextCardId(), ids)
        dictionary.cards[record.id] = record
        dictionaries.flush(dictionaryId)
        return CardEntityDbResponse(card = record.toEntity())
    }

    override fun updateCard(card: CardEntity): CardEntityDbResponse {
        requireExiting(card)
        val dictionaryId = card.dictionaryId.asDbId()
        val dictionary =
            dictionaries[dictionaryId] ?: return createNoDictionaryResponseError(card.dictionaryId, "updateCard")
        val id = card.cardId.asDbId()
        if (!dictionary.cards.containsKey(id)) {
            return createNoCardResponseError(card.cardId, "updateCard")
        }
        val record = card.toDbRecord(id, ids)
        dictionary.cards[record.id] = record
        dictionaries.flush(dictionaryId)
        return CardEntityDbResponse(card = record.toEntity())
    }

    override fun learnCards(learn: List<CardLearn>): CardEntitiesDbResponse {
        val cards = mutableSetOf<Card>()
        val errors = mutableListOf<AppError>()
        learn.forEach { cardLearn ->
            learnCard(cardLearn, errors)?.let { cards.add(it) }
        }
        cards.map { it.dictionaryId }.distinct().forEach {
            dictionaries.flush(it)
        }
        return CardEntitiesDbResponse(cards = cards.map { it.toEntity() }, errors = errors)
    }

    override fun resetCard(id: CardId): CardEntityDbResponse {
        val card = findCard(id) ?: return createNoCardResponseError(id, "resetCard")
        val record = card.copy(answered = 0)
        val dictionary = dictionaries[card.dictionaryId]
        requireNotNull(dictionary).cards[card.id] = record
        dictionaries.flush(card.dictionaryId)
        return CardEntityDbResponse(card = record.toEntity())
    }

    override fun deleteCard(id: CardId): DeleteEntityDbResponse {
        val card = findCard(id) ?: return DeleteEntityDbResponse(
            errors = listOf(
                noCardFoundDbError(operation = "deleteCard", id = id)
            )
        )
        val dictionary = dictionaries[card.dictionaryId]
        requireNotNull(dictionary).cards.remove(card.id)
        dictionaries.flush(card.dictionaryId)
        return DeleteEntityDbResponse()
    }

    private fun learnCard(learn: CardLearn, errors: MutableList<AppError>): Card? {
        val id = learn.cardId
        val card = findCard(id)
        if (card == null) {
            errors.add(noCardFoundDbError(operation = "learnCard", id = id))
            return null
        }
        val record = card.copy(details = toDbRecordDetails(learn.details))
        val dictionary = requireNotNull(dictionaries[card.dictionaryId])
        dictionary.cards[record.id] = record
        return record
    }

    private fun findCard(id: CardId): Card? {
        return dictionaries.keys.mapNotNull { dictionaries[it] }.mapNotNull { it.cards[id.asDbId()] }.singleOrNull()
    }

    private fun createNoCardResponseError(id: CardId, operation: String): CardEntityDbResponse {
        return CardEntityDbResponse(
            card = CardEntity.EMPTY,
            errors = listOf(noCardFoundDbError(operation = operation, id = id))
        )
    }

    private fun createNoDictionaryResponseError(id: DictionaryId, operation: String): CardEntityDbResponse {
        return CardEntityDbResponse(
            card = CardEntity.EMPTY,
            errors = listOf(noDictionaryFoundDbError(operation = operation, id = id))
        )
    }

}

