package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.dbcommon.DbUserRepositoryTest
import org.junit.jupiter.api.Order

@Order(1)
internal class PgDbUserRepositoryTest : DbUserRepositoryTest() {
    override val repository = PgDbUserRepository(PgTestContainer.config)
}