package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbCardRepository {
    private val database = MemDatabase.get(dbConfig.dataLocation)

    override fun findCardById(cardId: String): DbCard? {
        require(cardId.isNotBlank())
        return database.findCardById(cardId.toLong())?.toCardEntity()
    }

    override fun findCardsByDictionaryId(dictionaryId: String): Sequence<DbCard> {
        require(dictionaryId.isNotBlank())
        return database.findCardsByDictionaryId(dictionaryId.toLong()).map { it.toCardEntity() }
    }

    override fun createCard(cardEntity: DbCard): DbCard {
        validateCardEntityForCreate(cardEntity)
        val timestamp = systemNow()
        return try {
            database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
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
        return database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
    }

    override fun deleteCard(cardId: String): DbCard {
        val timestamp = systemNow()
        val found = database.findCardById(cardId.toLong())
            ?: throw DbDataException("Can't find card, id = ${cardId.toLong()}")
        if (!database.deleteCardById(cardId.toLong())) {
            throw DbDataException("Can't delete card, id = ${cardId.toLong()}")
        }
        return found.copy(changedAt = timestamp).toCardEntity()
    }
}