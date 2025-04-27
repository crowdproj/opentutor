package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbcommon.DbDocumentRepositoryTest
import com.gitlab.sszuev.flashcards.repositories.DbCardRepository
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DbDocumentRepository

class MemDbDocumentRepositoryTest : DbDocumentRepositoryTest() {
    override val documentRepository: DbDocumentRepository =
        MemDbDocumentRepository(dbConfig = MemDbConfig(dataLocation = "classpath:/db-mem-test-data"))
    override val dictionaryRepository: DbDictionaryRepository =
        MemDbDictionaryRepository(dbConfig = MemDbConfig(dataLocation = "classpath:/db-mem-test-data"))
    override val cardRepository: DbCardRepository =
        MemDbCardRepository(dbConfig = MemDbConfig(dataLocation = "classpath:/db-mem-test-data"))
}