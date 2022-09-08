package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbcommon.DbDictionaryRepositoryTest
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository

internal class MemDbDictionaryRepositoryTest : DbDictionaryRepositoryTest() {
    override val repository: DbDictionaryRepository = MemDbDictionaryRepository()
}