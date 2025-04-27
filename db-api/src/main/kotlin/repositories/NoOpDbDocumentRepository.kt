package com.gitlab.sszuev.flashcards.repositories

object NoOpDbDocumentRepository : DbDocumentRepository {
    override fun save(
        dictionary: DbDictionary,
        cards: List<DbCard>
    ) = noOp()

    private fun noOp(): Nothing {
        error("Must not be called.")
    }
}