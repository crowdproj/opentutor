package com.gitlab.sszuev.flashcards.services.impl

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.CardCorProcessor
import com.gitlab.sszuev.flashcards.services.CardService

/**
 * @see CardCorProcessor
 */
class CardServiceImpl : CardService {
    private val processor = CardCorProcessor()

    override suspend fun getResource(context: CardContext): CardContext = context.exec()

    override suspend fun createCard(context: CardContext): CardContext = context.exec()

    override suspend fun updateCard(context: CardContext): CardContext = context.exec()

    override suspend fun searchCards(context: CardContext): CardContext = context.exec()

    override suspend fun getAllCards(context: CardContext): CardContext = context.exec()

    override suspend fun getCard(context: CardContext): CardContext = context.exec()

    override suspend fun learnCard(context: CardContext): CardContext = context.exec()

    override suspend fun resetCard(context: CardContext): CardContext = context.exec()

    override suspend fun deleteCard(context: CardContext): CardContext = context.exec()

    private suspend fun CardContext.exec(): CardContext {
        processor.execute(this)
        return this
    }
}