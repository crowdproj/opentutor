package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.repositories.CardDbRepository
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.DictionaryIdDbRequest

class MemCardDbRepository : CardDbRepository {
    override fun getAllCards(dictionaryId: DictionaryIdDbRequest): CardEntitiesDbResponse {
        TODO("Not yet implemented")
    }
}