package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.common.validateCardEntityForCreate
import com.gitlab.sszuev.flashcards.common.validateCardEntityForUpdate
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDataException

class MemDbCardRepository(
    dbConfig: MemDbConfig = MemDbConfig(),
) : DbCardRepository {
    private val database = MemDatabase.get(dbConfig.dataLocation)

    override fun findCardById(cardId: CardId): CardEntity? {
        require(cardId != CardId.NONE)
        return database.findCardById(cardId.asLong())?.toCardEntity()
    }

    override fun findCardsByDictionaryId(dictionaryId: DictionaryId): Sequence<CardEntity> {
        require(dictionaryId != DictionaryId.NONE)
        return database.findCardsByDictionaryId(dictionaryId.asLong()).map { it.toCardEntity() }
    }

    override fun createCard(cardEntity: CardEntity): CardEntity {
        validateCardEntityForCreate(cardEntity)
        val timestamp = systemNow()
        return try {
            database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
        } catch (ex: Exception) {
            throw DbDataException("Can't create card $cardEntity", ex)
        }
    }

    override fun updateCard(cardEntity: CardEntity): CardEntity {
        validateCardEntityForUpdate(cardEntity)
        val found = database.findCardById(cardEntity.cardId.asLong())
            ?: throw DbDataException("Can't find card, id = ${cardEntity.cardId.asLong()}")
        if (found.dictionaryId != cardEntity.dictionaryId.asLong()) {
            throw DbDataException("Changing dictionary-id is not allowed; card id = ${cardEntity.cardId.asLong()}")
        }
        val timestamp = systemNow()
        return database.saveCard(cardEntity.toMemDbCard().copy(changedAt = timestamp)).toCardEntity()
    }

    override fun deleteCard(cardId: CardId): CardEntity {
        val timestamp = systemNow()
        val found = database.findCardById(cardId.asLong())
            ?: throw DbDataException("Can't find card, id = ${cardId.asLong()}")
        if (!database.deleteCardById(cardId.asLong())) {
            throw DbDataException("Can't delete card, id = ${cardId.asLong()}")
        }
        return found.copy(changedAt = timestamp).toCardEntity()
    }
}