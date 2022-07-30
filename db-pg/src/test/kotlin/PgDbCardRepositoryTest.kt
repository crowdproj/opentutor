package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbcommon.DbCardRepositoryTest

class PgDbCardRepositoryTest : DbCardRepositoryTest() {
    override val repository = PgDbCardRepository(PgTestContainer.config)
}