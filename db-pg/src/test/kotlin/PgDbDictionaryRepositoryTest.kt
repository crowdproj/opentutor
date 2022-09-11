package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbcommon.DbDictionaryRepositoryTest

class PgDbDictionaryRepositoryTest : DbDictionaryRepositoryTest() {
    override val repository = PgDbDictionaryRepository(PgTestContainer.config)
}