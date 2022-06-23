package com.gitlab.sszuev.flashcards.services.impl

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.CardCorProcessor
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.services.CardService
import kotlinx.datetime.Clock

/**
 * TODO: replace the logic will ChainOfResponsibility-based implementation.
 * @see CardCorProcessor
 */
class CardServiceImpl : CardService {
    private val processor = CardCorProcessor()
    private val stubs = StubsCardServiceImpl()

    override suspend fun createCard(context: CardContext): CardContext = context.exec()

    override suspend fun updateCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.updateCard(context)
        }
    }

    override suspend fun searchCards(context: CardContext): CardContext {
        return exec(context) {
            stubs.searchCards(context)
        }
    }

    override suspend fun getCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.getCard(context)
        }
    }

    override suspend fun learnCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.learnCard(context)
        }
    }

    override suspend fun resetCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.resetCard(context)
        }
    }

    override suspend fun deleteCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.deleteCard(context)
        }
    }

    @Deprecated("Will be removed: switch to CoR processor")
    private suspend fun exec(
        context: CardContext,
        stub: suspend (CardContext) -> CardContext
    ): CardContext {
        context.timestamp = Clock.System.now()
        return when (context.workMode) {
            AppMode.PROD, AppMode.TEST -> TODO()
            AppMode.STUB -> {
                stub.invoke(context)
            }
        }
    }

    private suspend fun CardContext.exec(): CardContext {
        processor.execute(this)
        return this
    }
}