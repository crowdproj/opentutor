package com.gitlab.sszuev.flashcards.repositories

/**
 * Database repository to work with cards.
 */
interface DbCardRepository {

    /**
     * Finds card by id returning `null` if nothing found.
     */
    fun findCardById(cardId: String): DbCard?

    /**
     * Finds cards by dictionary id.
     */
    fun findCardsByDictionaryId(dictionaryId: String): Sequence<DbCard>

    /**
     * Finds cards by dictionary ids.
     */
    fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<String>): Sequence<DbCard> =
        dictionaryIds.asSequence().flatMap { findCardsByDictionaryId(it) }

    /**
     * Finds cards by card ids.
     */
    fun findCardsByIdIn(cardIds: Iterable<String>): Sequence<DbCard> =
        cardIds.asSequence().mapNotNull { findCardById(it) }

    /**
     * Creates a new card returning the corresponding new card record from the db.
     * @throws IllegalArgumentException if the specified card has card id or illegal structure
     * @throws DbDataException in case card cannot be created for some reason,
     * i.e., if the corresponding dictionary does not exist
     */
    fun createCard(cardEntity: DbCard): DbCard

    /**
     * Performs bulk create.
     */
    fun createCards(cardEntities: Iterable<DbCard>): List<DbCard> = cardEntities.map { createCard(it) }

    /**
     * Updates the card entity.
     * @throws IllegalArgumentException if the specified card has no card id or has illegal structure
     * @throws DbDataException in case card cannot be created for some reason,
     * i.e., if the corresponding dictionary does not exist
     */
    fun updateCard(cardEntity: DbCard): DbCard

    /**
     * Performs bulk update.
     */
    fun updateCards(cardEntities: Iterable<DbCard>): List<DbCard> = cardEntities.map { updateCard(it) }

    /**
     * Deletes the card from the database, returning records that were deleted.
     */
    fun deleteCard(cardId: String): DbCard

    /**
     * Returns the number of cards for the specified dictionary.
     */
    fun countCardsByDictionaryId(dictionaryIds: Iterable<String>): Map<String, Long>

    /**
     * Returns the number of answered cards (`card.answered` >= [greaterOrEqual]) for the specified dictionary.
     */
    fun countCardsByDictionaryIdAndAnswered(dictionaryIds: Iterable<String>, greaterOrEqual: Int): Map<String, Long>
}