package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.api.services.CardService
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import io.ktor.server.application.*
import kotlinx.datetime.Clock

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.api.controllers.CardControllerKt")

suspend fun ApplicationCall.getResource(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<GetAudioRequest>(CardOperation.GET_RESOURCE, repositories, logger, runConf) {
        service.getResource(this)
    }
}

suspend fun ApplicationCall.createCard(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<CreateCardRequest>(CardOperation.CREATE_CARD, repositories, logger, runConf) {
        service.createCard(this)
    }
}

suspend fun ApplicationCall.updateCard(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<UpdateCardRequest>(CardOperation.UPDATE_CARD, repositories, logger, runConf) {
        service.updateCard(this)
    }
}

suspend fun ApplicationCall.searchCards(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<SearchCardsRequest>(CardOperation.SEARCH_CARDS, repositories, logger, runConf) {
        service.searchCards(this)
    }
}

suspend fun ApplicationCall.getAllCards(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<GetAllCardsRequest>(CardOperation.GET_ALL_CARDS, repositories, logger, runConf) {
        service.getAllCards(this)
    }
}

suspend fun ApplicationCall.getCard(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<GetCardRequest>(CardOperation.GET_CARD, repositories, logger, runConf) {
        service.getCard(this)
    }
}

suspend fun ApplicationCall.learnCard(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<LearnCardsRequest>(CardOperation.LEARN_CARDS, repositories, logger, runConf) {
        service.learnCard(this)
    }
}

suspend fun ApplicationCall.resetCard(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<ResetCardRequest>(CardOperation.RESET_CARD, repositories, logger, runConf) {
        service.resetCard(this)
    }
}

suspend fun ApplicationCall.deleteCard(service: CardService, repositories: CardRepositories, runConf: RunConfig) {
    execute<DeleteCardRequest>(CardOperation.DELETE_CARD, repositories, logger, runConf) {
        service.deleteCard(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: CardOperation,
    repositories: CardRepositories,
    logger: ExtLogger,
    runConf: RunConfig,
    noinline exec: suspend CardContext.() -> Unit,
) {
    val context = CardContext(
        operation = operation,
        timestamp = Clock.System.now(),
        repositories = repositories
    )
    context.fromUserTransport(runConf.auth)
    execute<R, CardContext>(operation, context, logger, exec)
}