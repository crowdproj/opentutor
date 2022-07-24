package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbcommon.CardDbRepositoryTest
import com.gitlab.sszuev.flashcards.repositories.CardDbRepository

class MemCardDbRepositoryImplTest : CardDbRepositoryTest() {
    override val repository: CardDbRepository = MemCardDbRepositoryImpl()
}