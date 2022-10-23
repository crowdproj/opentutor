package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbcommon.DbDictionaryRepositoryTest
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import org.junit.jupiter.api.Order

@Order(42)
internal class MemDbDictionaryRepositoryTest : DbDictionaryRepositoryTest() {
    override val repository: DbDictionaryRepository = MemDbDictionaryRepository()
}