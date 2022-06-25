package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.mappers.v1.*
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock

suspend fun ApplicationCall.createCard(service: CardService) {
    execute<CreateCardRequest>(CardOperation.CREATE_CARD) {
        service.createCard(this)
    }
}

suspend fun ApplicationCall.updateCard(service: CardService) {
    val updateCardRequest = receive<UpdateCardRequest>()
    respond(
        CardContext().apply {
            fromUpdateCardRequest(updateCardRequest)
        }.let {
            service.updateCard(it)
        }.toUpdateCardResponse()
    )
}

suspend fun ApplicationCall.searchCards(service: CardService) {
    execute<GetCardsRequest>(CardOperation.SEARCH_CARDS) {
        service.searchCards(this)
    }
}

suspend fun ApplicationCall.getCard(service: CardService) {
    val getCardRequest = receive<GetCardRequest>()
    respond(
        CardContext().apply {
            fromGetCardRequest(getCardRequest)
        }.let {
            service.getCard(it)
        }.toGetCardResponse()
    )
}

suspend fun ApplicationCall.learnCard(service: CardService) {
    execute<LearnCardRequest>(CardOperation.LEARN_CARD) {
        service.learnCard(this)
    }
}

suspend fun ApplicationCall.resetCard(service: CardService) {
    val resetCardRequest = receive<ResetCardRequest>()
    respond(
        CardContext().apply {
            fromResetCardRequest(resetCardRequest)
        }.let {
            service.resetCard(it)
        }.toResetCardResponse()
    )
}

suspend fun ApplicationCall.deleteCard(service: CardService) {
    val deleteCardRequest = receive<DeleteCardRequest>()
    respond(
        CardContext().apply {
            fromDeleteCardRequest(deleteCardRequest)
        }.let {
            service.deleteCard(it)
        }.toDeleteCardResponse()
    )
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: CardOperation? = null,
    exec: CardContext.() -> Unit
) {
    val context = CardContext()
    context.timestamp = Clock.System.now()
    try {
        val request = receive<R>()
        context.fromTransport(request)
        context.exec()
        val response = context.toResponse()
        respond(response)
    } catch (ex: Throwable) {
        operation?.also { context.operation = it }
        context.status = AppStatus.FAIL
        context.exec()
        val response = context.toResponse()
        respond(response)
    }
}