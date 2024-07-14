package com.gitlab.sszuev.flashcards.repositories

object NoOpDbCardRepository : DbCardRepository {

    override fun findCardById(cardId: String): DbCard = noOp()

    override fun findCardsByDictionaryId(dictionaryId: String): Sequence<DbCard> = noOp()

    override fun findCardsByDictionaryIdIn(dictionaryIds: Iterable<String>): Sequence<DbCard> = noOp()

    override fun findCardsByIdIn(cardIds: Iterable<String>): Sequence<DbCard> = noOp()

    override fun createCard(cardEntity: DbCard): DbCard = noOp()

    override fun updateCard(cardEntity: DbCard): DbCard = noOp()

    override fun updateCards(cardEntities: Iterable<DbCard>): List<DbCard> = noOp()

    override fun deleteCard(cardId: String): DbCard = noOp()

    override fun countCardsByDictionaryId(dictionaryIds: Iterable<String>): Map<String, Long> = noOp()

    override fun countCardsByDictionaryIdAndAnswered(
        dictionaryIds: Iterable<String>,
        greaterOrEqual: Int
    ): Map<String, Long> = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}