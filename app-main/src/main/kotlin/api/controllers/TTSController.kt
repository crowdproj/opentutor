package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioRequest
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.toAppConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation
import com.gitlab.sszuev.flashcards.services.TTSService
import io.ktor.server.application.ApplicationCall
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.api.controllers.TTSController")

suspend fun ApplicationCall.getResource(
    service: TTSService,
    contextConfig: ContextConfig
) {
    execute<GetAudioRequest>(TTSOperation.GET_RESOURCE, logger, contextConfig) {
        service.getResource(this)
    }
}

@OptIn(ExperimentalTime::class)
private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: TTSOperation,
    logger: ExtLogger,
    contextConfig: ContextConfig,
    noinline exec: suspend TTSContext.() -> Unit,
) {
    val context = TTSContext(
        operation = operation,
        timestamp = Clock.System.now(),
        config = contextConfig.toAppConfig(),
    )
    context.fromUserTransport(contextConfig.runConfig.auth)
    execute<R, TTSContext>(operation, context, logger, exec)
}