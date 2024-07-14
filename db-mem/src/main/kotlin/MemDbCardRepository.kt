package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.systemNow

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbCardRepository {
    private val database by lazy { MemDatabase.get(dbConfig.dataLocation) }

    override fun findCardById(cardId: String): DbCard? =
        database.findCardById(require(cardId.isNotBlank()).run { cardId.toLong() })?.toDbCard()

    override fun findCardsByDictionaryId(dictionaryId: String): Sequence<DbCard> =
        database.findCardsByDictionaryId(require(dictionaryId.isNotBlank()).run { dictionaryId.toLong() })
            .map { it.toDbCard() }

    override fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<String>): Sequence<DbCard> =
        database.findCardsByDictionaryIds(dictionaryIds.onEach { require(it.isNotBlank()) }.map { it.toLong() })
            .map { it.toDbCard() }

    override fun findCardsByIdIn(cardIds: Iterable<String>): Sequence<DbCard> =
        database.findCardsById(cardIds.onEach { require(it.isNotBlank()) }.map { it.toLong() })
            .map { it.toDbCard() }

    override fun createCard(cardEntity: DbCard): DbCard {
        validateCardEntityForCreate(cardEntity)
        val timestamp = systemNow()
        return try {
            database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toDbCard()
        } catch (ex: Exception) {
            throw DbDataException("Can't create card $cardEntity", ex)
        }
    }

    override fun updateCard(cardEntity: DbCard): DbCard {
        validateCardEntityForUpdate(cardEntity)
        val found = database.findCardById(cardEntity.cardId.toLong())
            ?: throw DbDataException("Can't find card, id = ${cardEntity.cardId.toLong()}")
        if (found.dictionaryId != cardEntity.dictionaryId.toLong()) {
            throw DbDataException("Changing dictionary-id is not allowed; card id = ${cardEntity.cardId.toLong()}")
        }
        val timestamp = systemNow()
        return database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toDbCard()
    }

    override fun deleteCard(cardId: String): DbCard {
        val timestamp = systemNow()
        val found = database.findCardById(cardId.toLong())
            ?: throw DbDataException("Can't find card, id = ${cardId.toLong()}")
        if (!database.deleteCardById(cardId.toLong())) {
            throw DbDataException("Can't delete card, id = ${cardId.toLong()}")
        }
        return found.copy(changedAt = timestamp).toDbCard()
    }

    override fun countCardsByDictionaryId(dictionaryIds: Iterable<String>): Map<String, Long> {
        val ids = dictionaryIds.toSet()
        return database.counts().mapKeys { it.key.toString() }.filterKeys { it in ids }
    }

    override fun countCardsByDictionaryIdAndAnswered(
        dictionaryIds: Iterable<String>,
        greaterOrEqual: Int
    ): Map<String, Long> {
        val ids = dictionaryIds.toSet()
        return database
            .counts { (it.answered ?: 0) >= greaterOrEqual }
            .mapKeys { it.key.toString() }
            .filterKeys { it in ids }
    }
}