package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbcommon.DbUserRepositoryTest
import com.gitlab.sszuev.flashcards.repositories.DbUserRepository

internal class MemDbUserRepositoryTest : DbUserRepositoryTest() {
    override val repository: DbUserRepository = MemDbUserRepository()
}