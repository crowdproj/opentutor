package com.gitlab.sszuev.flashcards.services.impl

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.services.CardService
import com.gitlab.sszuev.flashcards.services.errorResponse
import com.gitlab.sszuev.flashcards.services.successResponse
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.toStatus
import kotlin.random.Random

@Deprecated("will be removed in favour of CoR Processor")
internal class StubsCardServiceImpl : CardService {
    override suspend fun createCard(context: CardContext): CardContext {
        context.status = toStatus(context.debugCase)
        return when (context.status) {
            AppStatus.OK -> context.successResponse { responseCardEntity = requestCardEntity }
            else -> context.errorResponse({ stubError })
        }
    }

    override suspend fun updateCard(context: CardContext): CardContext {
        context.status = toStatus(context.debugCase)
        return when (context.status) {
            AppStatus.OK -> context.successResponse { responseCardEntity = requestCardEntity }
            else -> context.errorResponse({ stubError })
        }
    }

    override suspend fun searchCards(context: CardContext): CardContext {
        context.status = toStatus(context.debugCase)
        return when (context.status) {
            AppStatus.OK -> context.successResponse {
                if (requestCardFilter.length == 1) {
                    responseCardEntityList = listOf(stubCard)
                    return@successResponse
                }
                var cards = IntRange(1, requestCardFilter.length).map {
                    val dicId = with(requestCardFilter) {
                        if (dictionaryIds.isEmpty()) {
                            throw IllegalArgumentException()
                        }
                        if (random) { // non-deterministic
                            dictionaryIds[Random.Default.nextInt(dictionaryIds.size)]
                        } else { // deterministic:
                            dictionaryIds[0]
                        }
                    }
                    val cardId = CardId("card$it")
                    stubCard.copy(
                        cardId = cardId,
                        dictionaryId = dicId,
                        word = stubCard.word + it
                    )
                }.toList()
                if (requestCardFilter.random) {
                    cards = cards.shuffled(Random.Default)
                }
                responseCardEntityList = cards
            }
            else -> context.errorResponse({ stubError })
        }
    }

    override suspend fun getCard(context: CardContext): CardContext {
        context.status = toStatus(context.debugCase)
        return when (context.status) {
            AppStatus.OK -> context.successResponse {
                responseCardEntity = stubCard.copy(cardId = context.requestCardEntityId)
            }
            else -> context.errorResponse({ stubError })
        }
    }

    override suspend fun learnCard(context: CardContext): CardContext {
        context.status = toStatus(context.debugCase)
        return when (context.status) {
            AppStatus.OK -> context.successResponse { }
            else -> context.errorResponse({ stubError })
        }
    }

    override suspend fun resetCard(context: CardContext): CardContext {
        context.status = toStatus(context.debugCase)
        return when (context.status) {
            AppStatus.OK -> context.successResponse { }
            else -> context.errorResponse({ stubError })
        }
    }

    override suspend fun deleteCard(context: CardContext): CardContext {
        context.status = toStatus(context.debugCase)
        return when (context.status) {
            AppStatus.OK -> context.successResponse { }
            else -> context.errorResponse({ stubError })
        }
    }
}