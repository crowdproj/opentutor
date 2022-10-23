package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbcommon.DbCardRepositoryTest
import org.junit.jupiter.api.Order

@Order(1)
internal class PgDbCardRepositoryTest : DbCardRepositoryTest() {
    override val repository = PgDbCardRepository(PgTestContainer.config)
}