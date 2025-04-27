package com.gitlab.sszuev.flashcards.dbcommon.mocks

import com.gitlab.sszuev.flashcards.repositories.DbCard
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDocumentRepository

class MockDbDocumentRepository(
    private val invokeSave: (DbDictionary, List<DbCard>) -> String = { _, _ -> "" },
) : DbDocumentRepository {

    override fun save(
        dictionary: DbDictionary,
        cards: List<DbCard>
    ): String = invokeSave(dictionary, cards)

}