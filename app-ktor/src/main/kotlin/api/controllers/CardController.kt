package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.mappers.v1.fromTransport
import com.gitlab.sszuev.flashcards.mappers.v1.toResponse
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

suspend fun ApplicationCall.getResource(service: CardService) {
    execute<GetAudioRequest>(CardOperation.GET_RESOURCE) {
        service.getResource(this)
    }
}

suspend fun ApplicationCall.createCard(service: CardService) {
    execute<CreateCardRequest>(CardOperation.CREATE_CARD) {
        service.createCard(this)
    }
}

suspend fun ApplicationCall.updateCard(service: CardService) {
    execute<UpdateCardRequest>(CardOperation.UPDATE_CARD) {
        service.updateCard(this)
    }
}

suspend fun ApplicationCall.searchCards(service: CardService) {
    execute<SearchCardsRequest>(CardOperation.SEARCH_CARDS) {
        service.searchCards(this)
    }
}

suspend fun ApplicationCall.getAllCards(service: CardService) {
    execute<GetAllCardsRequest>(CardOperation.GET_ALL_CARDS) {
        service.getAllCards(this)
    }
}

suspend fun ApplicationCall.getCard(service: CardService) {
    execute<GetCardRequest>(CardOperation.GET_CARD) {
        service.getCard(this)
    }
}

suspend fun ApplicationCall.learnCard(service: CardService) {
    execute<LearnCardsRequest>(CardOperation.LEARN_CARDS) {
        service.learnCard(this)
    }
}

suspend fun ApplicationCall.resetCard(service: CardService) {
    execute<ResetCardRequest>(CardOperation.RESET_CARD) {
        service.resetCard(this)
    }
}

suspend fun ApplicationCall.deleteCard(service: CardService) {
    execute<DeleteCardRequest>(CardOperation.DELETE_CARD) {
        service.deleteCard(this)
    }
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
    } catch (ex: Exception) {
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

private fun Exception.asError(
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