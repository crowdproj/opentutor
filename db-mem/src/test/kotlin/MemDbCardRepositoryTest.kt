package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbcommon.DbCardRepositoryTest
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository

internal class MemDbCardRepositoryTest : DbCardRepositoryTest() {
    override val repository: DbCardRepository = MemDbCardRepository()
}