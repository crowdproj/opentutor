package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.mappers.v1.*
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("CardController")

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
    execute<GetCardRequest>(CardOperation.GET_CARD) {
        service.getCard(this)
    }
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
        if (logger.isDebugEnabled) {
            logger.debug("Request: $operation")
        }
        val request = receive<R>()
        context.fromTransport(request)
        context.exec()
        val response = context.toResponse()
        respond(response)
    } catch (ex: Throwable) {
        val msg = "Problem with request=${context.requestId.asString()} :: ${ex.message}"
        if (logger.isDebugEnabled) {
            logger.debug(msg, ex)
        }
        operation?.also { context.operation = it }
        context.status = AppStatus.FAIL
        context.errors.add(ex.asError(message = msg))
        val response = context.toResponse()
        respond(response)
    }
}

private fun Throwable.asError(
    code: String = "unknown",
    group: String = "exceptions",
    message: String = this.message ?: "",
) = AppError(
    code = code,
    group = group,
    field = "",
    message = message,
    exception = this,
)