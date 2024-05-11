package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.CardCorProcessor
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.remoteDbRepositories

class RemoteCardService : CardService {
    private val processor = CardCorProcessor()

    override suspend fun createCard(context: CardContext): CardContext = context.exec()
    override suspend fun updateCard(context: CardContext): CardContext = context.exec()
    override suspend fun searchCards(context: CardContext): CardContext = context.exec()
    override suspend fun getAllCards(context: CardContext): CardContext = context.exec()
    override suspend fun getCard(context: CardContext): CardContext = context.exec()
    override suspend fun learnCard(context: CardContext): CardContext = context.exec()
    override suspend fun resetCard(context: CardContext): CardContext = context.exec()
    override suspend fun deleteCard(context: CardContext): CardContext = context.exec()

    private suspend fun CardContext.exec(): CardContext {
        this.repositories = remoteDbRepositories
        processor.execute(this)
        return this
    }
}