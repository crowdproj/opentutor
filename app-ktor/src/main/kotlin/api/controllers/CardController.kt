package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.CardRepositories
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.logmappers.toLogResource
import com.gitlab.sszuev.flashcards.logslib.LogbackWrapper
import com.gitlab.sszuev.flashcards.mappers.v1.fromTransportToRequest
import com.gitlab.sszuev.flashcards.mappers.v1.fromTransportToUser
import com.gitlab.sszuev.flashcards.mappers.v1.toResponse
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.services.CardService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock

suspend fun ApplicationCall.getResource(service: CardService, logger: LogbackWrapper) {
    execute<GetAudioRequest>(CardOperation.GET_RESOURCE, service.repositories(), logger) {
        service.getResource(this)
    }
}

suspend fun ApplicationCall.createCard(service: CardService, logger: LogbackWrapper) {
    execute<CreateCardRequest>(CardOperation.CREATE_CARD, service.repositories(), logger) {
        service.createCard(this)
    }
}

suspend fun ApplicationCall.updateCard(service: CardService, logger: LogbackWrapper) {
    execute<UpdateCardRequest>(CardOperation.UPDATE_CARD, service.repositories(), logger) {
        service.updateCard(this)
    }
}

suspend fun ApplicationCall.searchCards(service: CardService, logger: LogbackWrapper) {
    execute<SearchCardsRequest>(CardOperation.SEARCH_CARDS, service.repositories(), logger) {
        service.searchCards(this)
    }
}

suspend fun ApplicationCall.getAllCards(service: CardService, logger: LogbackWrapper) {
    execute<GetAllCardsRequest>(CardOperation.GET_ALL_CARDS, service.repositories(), logger) {
        service.getAllCards(this)
    }
}

suspend fun ApplicationCall.getCard(service: CardService, logger: LogbackWrapper) {
    execute<GetCardRequest>(CardOperation.GET_CARD, service.repositories(), logger) {
        service.getCard(this)
    }
}

suspend fun ApplicationCall.learnCard(service: CardService, logger: LogbackWrapper) {
    execute<LearnCardsRequest>(CardOperation.LEARN_CARDS, service.repositories(), logger) {
        service.learnCard(this)
    }
}

suspend fun ApplicationCall.resetCard(service: CardService, logger: LogbackWrapper) {
    execute<ResetCardRequest>(CardOperation.RESET_CARD, service.repositories(), logger) {
        service.resetCard(this)
    }
}

suspend fun ApplicationCall.deleteCard(service: CardService, logger: LogbackWrapper) {
    execute<DeleteCardRequest>(CardOperation.DELETE_CARD, service.repositories(), logger) {
        service.deleteCard(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: CardOperation,
    repositories: CardRepositories,
    logger: LogbackWrapper,
    noinline exec: suspend CardContext.() -> Unit,
) {
    val context = CardContext(operation = operation, timestamp = Clock.System.now(), repositories = repositories)
    val logId = operation.name
    try {
        logger.withLogging {
            val principal = requireNotNull(principal<JWTPrincipal>()) {
                "No principal in request"
            }
            val requestUserUid = requireNotNull(principal.subject) {
                "No subject in principal=$principal"
            }
            val request = receive<R>()
            context.fromTransportToRequest(request)
            context.fromTransportToUser(requestUserUid)
            logger.info(msg = "Request: $operation", data = context.toLogResource(logId))
            context.exec()
            logger.info(msg = "Response: $operation", data = context.toLogResource(logId))
            val response = context.toResponse()
            respond(response)
        }
    } catch (ex: Exception) {
        val msg = "Problem with request=${context.requestId.asString()} :: ${ex.message}"
        logger.error(msg = msg, throwable = ex, data = context.toLogResource(logId))
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