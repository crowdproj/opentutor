package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.common.noCardFoundDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.common.wrongDictionaryLanguageFamilies
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DeleteCardDbResponse
import java.time.LocalDateTime
import kotlin.random.Random

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbCardRepository {
    private val database = MemDatabase.get(dbConfig.dataLocation)

    override fun getCard(cardId: CardId): CardDbResponse {
        val card = database.findCardById(cardId.asLong()) ?: return CardDbResponse(
            card = CardEntity.EMPTY,
            errors = listOf(noCardFoundDbError(operation = "getCard", id = cardId))
        )
        return CardDbResponse(card = card.toCardEntity())
    }

    override fun getAllCards(dictionaryId: DictionaryId): CardsDbResponse {
        val id = dictionaryId.asLong()
        val dictionary = database.findDictionaryById(id)
            ?: return CardsDbResponse(
                cards = emptyList(),
                errors = listOf(noDictionaryFoundDbError(operation = "getAllCards", id = dictionaryId))
            )
        val cards = database.findCardsByDictionaryId(id).map { it.toCardEntity() }.toList()
        return CardsDbResponse(
            cards = cards,
            sourceLanguageId = dictionary.sourceLanguage.toLangEntity().langId,
            errors = emptyList()
        )
    }

    override fun searchCard(filter: CardFilter): CardsDbResponse {
        val ids = filter.dictionaryIds.map { it.asLong() }
        val dictionaries = database.findDictionariesByIds(ids).sortedBy { it.id }.toSet()
        if (dictionaries.isEmpty()) {
            return CardsDbResponse(cards = emptyList())
        }
        val sourceLanguages = dictionaries.map { it.sourceLanguage.toLangEntity().langId }.toSet()
        val targetLanguages = dictionaries.map { it.targetLanguage.toLangEntity().langId }.toSet()
        if (sourceLanguages.size != 1 || targetLanguages.size != 1) {
            return CardsDbResponse(
                cards = emptyList(),
                errors = listOf(
                    wrongDictionaryLanguageFamilies(operation = "searchCard", dictionaryIds = filter.dictionaryIds)
                )
            )
        }
        var cardsFromDb = database.findCardsByDictionaryIds(ids)
        if (!filter.withUnknown) {
            cardsFromDb = cardsFromDb.filter { sysConfig.status(it.answered) != DocumentCardStatus.LEARNED }
        }
        if (filter.random) {
            cardsFromDb = cardsFromDb.shuffled(Random.Default)
        }
        val cards = cardsFromDb.take(filter.length).map { it.toCardEntity() }.toList()
        return CardsDbResponse(cards = cards, sourceLanguageId = sourceLanguages.single())
    }

    override fun createCard(cardEntity: CardEntity): CardDbResponse {
        validateCardEntityForCreate(cardEntity)
        val dictionaryId = cardEntity.dictionaryId.asLong()
        database.findDictionaryById(dictionaryId) ?: return createNoDictionaryResponseError(
            id = cardEntity.dictionaryId,
            operation = "createCard"
        )
        return CardDbResponse(card = database.saveCard(cardEntity.toMemDbCard()).toCardEntity())
    }

    override fun updateCard(cardEntity: CardEntity): CardDbResponse {
        validateCardEntityForUpdate(cardEntity)
        val dictionaryId = cardEntity.dictionaryId.asLong()
        database.findDictionaryById(dictionaryId) ?: return createNoDictionaryResponseError(
            id = cardEntity.dictionaryId,
            operation = "updateCard"
        )
        val cardId = cardEntity.cardId.asLong()
        database.findCardById(cardId) ?: return createNoCardResponseError(cardEntity.cardId, "updateCard")
        return CardDbResponse(
            card = database.saveCard(cardEntity.toMemDbCard().copy(changedAt = LocalDateTime.now())).toCardEntity()
        )
    }

    override fun learnCards(cardLearn: List<CardLearn>): CardsDbResponse {
        val timestamp = LocalDateTime.now()
        val errors = mutableListOf<AppError>()
        val cards = cardLearn.mapNotNull { learnCard(it, errors, timestamp) }
        return CardsDbResponse(cards = cards.map { it.toCardEntity() }, errors = errors)
    }

    override fun resetCard(cardId: CardId): CardDbResponse {
        val card = database.findCardById(cardId.asLong()) ?: return createNoCardResponseError(cardId, "resetCard")
        val res = database.saveCard(card.copy(answered = 0, changedAt = LocalDateTime.now()))
        return CardDbResponse(card = res.toCardEntity())
    }

    override fun deleteCard(cardId: CardId): DeleteCardDbResponse {
        return if (database.deleteCardById(cardId.asLong())) {
            DeleteCardDbResponse()
        } else {
            DeleteCardDbResponse(errors = listOf(noCardFoundDbError(operation = "deleteCard", id = cardId)))
        }
    }

    private fun learnCard(
        learn: CardLearn,
        errors: MutableList<AppError>,
        changeAt: LocalDateTime,
    ): MemDbCard? {
        val id = learn.cardId
        val card = database.findCardById(id.asLong())
        if (card == null) {
            errors.add(noCardFoundDbError(operation = "learnCard", id = id))
            return null
        }
        val record = card.copy(details = learn.details.toMemDbCardDetails(), changedAt = changeAt)
        return database.saveCard(record)
    }

    private fun createNoCardResponseError(id: CardId, operation: String): CardDbResponse {
        return CardDbResponse(
            card = CardEntity.EMPTY,
            errors = listOf(noCardFoundDbError(operation = operation, id = id))
        )
    }

    private fun createNoDictionaryResponseError(id: DictionaryId, operation: String): CardDbResponse {
        return CardDbResponse(
            card = CardEntity.EMPTY,
            errors = listOf(noDictionaryFoundDbError(operation = operation, id = id))
        )
    }
}

