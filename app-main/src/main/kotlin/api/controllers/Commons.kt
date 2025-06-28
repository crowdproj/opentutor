package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.logmappers.toLogResource
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.mappers.v1.fromTransport
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransportIfRequired
import com.gitlab.sszuev.flashcards.mappers.v1.toResponse
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppOperation
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.slf4j.event.Level

internal suspend inline fun <reified Request : BaseRequest, reified Context : AppContext> ApplicationCall.execute(
    operation: AppOperation,
    context: Context,
    logger: ExtLogger,
    noinline exec: suspend Context.() -> Unit,
) {
    val logId = operation.name
    try {
        logger.withLogging(level = Level.DEBUG) {
            context.fromUserTransportIfRequired { requestAuthId() }
            context.fromTransport(receive<Request>())
            logger.info(msg = "Request: $operation", data = context.toLogResource(logId))
            context.exec()
            if (context.status == AppStatus.FAIL) {
                logger.error(
                    msg = "$operation :: errors: ${context.errors.map { it.message }}",
                    throwable = context.errors.throwable(operation),
                    data = context.toLogResource(logId)
                )
            } else {
                logger.info(msg = "Response: $operation", data = context.toLogResource(logId))
            }
            val response = context.toResponse()
            respond(response)
        }
    } catch (ex: Exception) {
        val msg = "Problem with request='${context.requestId.asString()}' :: ${ex.message}"
        context.status = AppStatus.FAIL
        context.errors.add(ex.asError(message = msg))
        logger.error(
            msg = "$operation :: exceptions: ${context.errors.map { it.message }}",
            throwable = context.errors.throwable(operation),
            data = context.toLogResource(logId)
        )
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

private fun Collection<AppError>.throwable(operation: AppOperation): Throwable? {
    val exceptions = this.mapNotNull { it.exception }
    if (exceptions.isEmpty()) return null
    if (exceptions.size == 1) {
        return exceptions.first()
    }
    val res = RuntimeException("[$operation] multiple exceptions")
    exceptions.forEach { res.addSuppressed(it) }
    return res
}