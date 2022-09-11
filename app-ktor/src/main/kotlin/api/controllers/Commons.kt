package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.logmappers.toLogResource
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.mappers.v1.fromTransport
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.mappers.v1.toResponse
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppOperation
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*

internal suspend inline fun <reified Request : BaseRequest, reified Context : AppContext> ApplicationCall.execute(
    operation: AppOperation,
    context: Context,
    logger: ExtLogger,
    noinline exec: suspend Context.() -> Unit,
) {
    val logId = operation.name
    try {
        logger.withLogging {
            val requestUserUid = requestAuthId()
            val request = receive<Request>()
            context.fromTransport(request)
            context.fromUserTransport(requestUserUid)
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

internal fun ApplicationCall.requestAuthId(): String {
    val principal = requireNotNull(principal<JWTPrincipal>()) {
        "No principal in request"
    }
    return requireNotNull(principal.subject) {
        "No subject in principal=$principal"
    }
}

internal fun Exception.asError(
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