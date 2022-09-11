package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import kotlinx.datetime.Clock

suspend fun ApplicationCall.getResource(service: CardService, logger: ExtLogger) {
    execute<GetAudioRequest>(CardOperation.GET_RESOURCE, service.repositories(), logger) {
        service.getResource(this)
    }
}

suspend fun ApplicationCall.createCard(service: CardService, logger: ExtLogger) {
    execute<CreateCardRequest>(CardOperation.CREATE_CARD, service.repositories(), logger) {
        service.createCard(this)
    }
}

suspend fun ApplicationCall.updateCard(service: CardService, logger: ExtLogger) {
    execute<UpdateCardRequest>(CardOperation.UPDATE_CARD, service.repositories(), logger) {
        service.updateCard(this)
    }
}

suspend fun ApplicationCall.searchCards(service: CardService, logger: ExtLogger) {
    execute<SearchCardsRequest>(CardOperation.SEARCH_CARDS, service.repositories(), logger) {
        service.searchCards(this)
    }
}

suspend fun ApplicationCall.getAllCards(service: CardService, logger: ExtLogger) {
    execute<GetAllCardsRequest>(CardOperation.GET_ALL_CARDS, service.repositories(), logger) {
        service.getAllCards(this)
    }
}

suspend fun ApplicationCall.getCard(service: CardService, logger: ExtLogger) {
    execute<GetCardRequest>(CardOperation.GET_CARD, service.repositories(), logger) {
        service.getCard(this)
    }
}

suspend fun ApplicationCall.learnCard(service: CardService, logger: ExtLogger) {
    execute<LearnCardsRequest>(CardOperation.LEARN_CARDS, service.repositories(), logger) {
        service.learnCard(this)
    }
}

suspend fun ApplicationCall.resetCard(service: CardService, logger: ExtLogger) {
    execute<ResetCardRequest>(CardOperation.RESET_CARD, service.repositories(), logger) {
        service.resetCard(this)
    }
}

suspend fun ApplicationCall.deleteCard(service: CardService, logger: ExtLogger) {
    execute<DeleteCardRequest>(CardOperation.DELETE_CARD, service.repositories(), logger) {
        service.deleteCard(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: CardOperation,
    repositories: CardRepositories,
    logger: ExtLogger,
    noinline exec: suspend CardContext.() -> Unit,
) {
    val context = CardContext(operation = operation, timestamp = Clock.System.now(), repositories = repositories)
    execute<R, CardContext>(operation, context, logger, exec)
}