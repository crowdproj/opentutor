package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

/**
 * Database repository to work with cards.
 */
interface DbCardRepository {

    /**
     * Finds card by id returning `null` if nothing found.
     */
    fun findCardById(cardId: CardId): CardEntity?

    /**
     * Finds cards by dictionary id.
     */
    fun findCardsByDictionaryId(dictionaryId: DictionaryId): Sequence<CardEntity>

    /**
     * Finds cards by dictionary ids.
     */
    fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<DictionaryId>): Sequence<CardEntity> =
        dictionaryIds.asSequence().flatMap { findCardsByDictionaryId(it) }

    /**
     * Finds cards by card ids.
     */
    fun findCardsByIdIn(cardIds: Iterable<CardId>): Sequence<CardEntity> =
        cardIds.asSequence().mapNotNull { findCardById(it) }

    /**
     * Creates a new card returning the corresponding new card record from the db.
     * @throws IllegalArgumentException if the specified card has card id or illegal structure
     * @throws DbDataException in case card cannot be created for some reason,
     * i.e., if the corresponding dictionary does not exist
     */
    fun createCard(cardEntity: CardEntity): CardEntity

    /**
     * Updates the card entity.
     * @throws IllegalArgumentException if the specified card has no card id or has illegal structure
     * @throws DbDataException in case card cannot be created for some reason,
     * i.e., if the corresponding dictionary does not exist
     */
    fun updateCard(cardEntity: CardEntity): CardEntity

    /**
     * Performs bulk update.
     */
    fun updateCards(cardEntities: Iterable<CardEntity>): List<CardEntity> = cardEntities.map { updateCard(it) }

    /**
     * Deletes card by id.
     */
    fun removeCard(userId: AppUserId, cardId: CardId): RemoveCardDbResponse

}

data class RemoveCardDbResponse(
    val card: CardEntity = CardEntity.EMPTY,
    val errors: List<AppError> = emptyList(),
) {
    constructor(error: AppError) : this(errors = listOf(error))

    companion object {
        val EMPTY = RemoveCardDbResponse(errors = emptyList())
    }
}