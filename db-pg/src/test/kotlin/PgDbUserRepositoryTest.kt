package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbcommon.DbUserRepositoryTest

internal class PgDbUserRepositoryTest : DbUserRepositoryTest() {
    override val repository = PgDbUserRepository(PgTestContainer.config)
}