package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbcommon.DbDocumentRepositoryTest
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbDocumentRepository

class PgDbDocumentRepositoryTest : DbDocumentRepositoryTest() {
    override val documentRepository: DbDocumentRepository = PgDbDocumentRepository(PgTestContainer.config)
    override val dictionaryRepository: DbDictionaryRepository = PgDbDictionaryRepository(PgTestContainer.config)
    override val cardRepository: DbCardRepository = PgDbCardRepository(PgTestContainer.config)
}