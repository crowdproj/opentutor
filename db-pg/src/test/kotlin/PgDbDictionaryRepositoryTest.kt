package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbcommon.DbDictionaryRepositoryTest
import org.junit.jupiter.api.Order

@Order(42)
class PgDbDictionaryRepositoryTest : DbDictionaryRepositoryTest() {
    override val repository = PgDbDictionaryRepository(PgTestContainer.config)
}