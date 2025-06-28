package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetSettingsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateSettingsRequest
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.toAppConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.services.SettingsService
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.Clock

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.api.controllers.SettingsController")

suspend fun ApplicationCall.getSettings(
    service: SettingsService,
    contextConfig: ContextConfig
) {
    execute<GetSettingsRequest>(
        operation = SettingsOperation.GET_SETTINGS,
        logger = logger,
        contextConfig = contextConfig,
    ) {
        service.getSettings(this)
    }
}

suspend fun ApplicationCall.updateSettings(
    service: SettingsService,
    contextConfig: ContextConfig
) {
    execute<UpdateSettingsRequest>(
        operation = SettingsOperation.UPDATE_SETTINGS,
        logger = logger,
        contextConfig = contextConfig,
    ) {
        service.updateSettings(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: SettingsOperation,
    logger: ExtLogger,
    contextConfig: ContextConfig,
    noinline exec: suspend SettingsContext.() -> Unit,
) {
    val context = SettingsContext(
        operation = operation,
        timestamp = Clock.System.now(),
        config = contextConfig.toAppConfig(),
    )
    context.fromUserTransport(contextConfig.runConfig.auth)
    execute<R, SettingsContext>(operation, context, logger, exec)
}