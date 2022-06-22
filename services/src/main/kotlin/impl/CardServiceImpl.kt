package com.gitlab.sszuev.flashcards.services.impl

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.services.CardService
import kotlinx.datetime.Clock

/**
 * TODO: replace the logic will ChainOfResponsibility-based implementation.
 */
class CardServiceImpl : CardService {
    private val stubs = StubsCardServiceImpl()

    override fun createCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.createCard(context)
        }
    }

    override fun updateCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.createCard(context)
        }
    }

    override fun searchCards(context: CardContext): CardContext {
        return exec(context) {
            stubs.searchCards(context)
        }
    }

    override fun getCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.getCard(context)
        }
    }

    override fun learnCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.learnCard(context)
        }
    }

    override fun resetCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.resetCard(context)
        }
    }

    override fun deleteCard(context: CardContext): CardContext {
        return exec(context) {
            stubs.deleteCard(context)
        }
    }

    private fun exec(
        context: CardContext,
        stub: (CardContext) -> CardContext
    ): CardContext {
        context.timestamp = Clock.System.now()
        return when (context.workMode) {
            AppMode.PROD, AppMode.TEST -> TODO()
            AppMode.STUB -> {
                stub.invoke(context)
            }
        }
    }
}