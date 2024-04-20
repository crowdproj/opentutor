package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.AppRepositories
import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.services.CardService
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardRequest
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.toAppConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.Clock

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.api.controllers.CardController")

suspend fun ApplicationCall.getResource(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<GetAudioRequest>(CardOperation.GET_RESOURCE, repositories, logger, contextConfig) {
        service.getResource(this)
    }
}

suspend fun ApplicationCall.createCard(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<CreateCardRequest>(CardOperation.CREATE_CARD, repositories, logger, contextConfig) {
        service.createCard(this)
    }
}

suspend fun ApplicationCall.updateCard(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<UpdateCardRequest>(CardOperation.UPDATE_CARD, repositories, logger, contextConfig) {
        service.updateCard(this)
    }
}

suspend fun ApplicationCall.searchCards(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<SearchCardsRequest>(CardOperation.SEARCH_CARDS, repositories, logger, contextConfig) {
        service.searchCards(this)
    }
}

suspend fun ApplicationCall.getAllCards(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<GetAllCardsRequest>(CardOperation.GET_ALL_CARDS, repositories, logger, contextConfig) {
        service.getAllCards(this)
    }
}

suspend fun ApplicationCall.getCard(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<GetCardRequest>(CardOperation.GET_CARD, repositories, logger, contextConfig) {
        service.getCard(this)
    }
}

suspend fun ApplicationCall.learnCard(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<LearnCardsRequest>(CardOperation.LEARN_CARDS, repositories, logger, contextConfig) {
        service.learnCard(this)
    }
}

suspend fun ApplicationCall.resetCard(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<ResetCardRequest>(CardOperation.RESET_CARD, repositories, logger, contextConfig) {
        service.resetCard(this)
    }
}

suspend fun ApplicationCall.deleteCard(
    service: CardService,
    repositories: AppRepositories,
    contextConfig: ContextConfig
) {
    execute<DeleteCardRequest>(CardOperation.DELETE_CARD, repositories, logger, contextConfig) {
        service.deleteCard(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: CardOperation,
    repositories: AppRepositories,
    logger: ExtLogger,
    contextConfig: ContextConfig,
    noinline exec: suspend CardContext.() -> Unit,
) {
    val context = CardContext(
        operation = operation,
        timestamp = Clock.System.now(),
        repositories = repositories,
        config = contextConfig.toAppConfig(),
    )
    context.fromUserTransport(contextConfig.runConfig.auth)
    execute<R, CardContext>(operation, context, logger, exec)
}