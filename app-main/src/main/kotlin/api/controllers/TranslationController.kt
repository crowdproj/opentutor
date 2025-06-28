package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.FetchTranslationRequest
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.toAppConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation
import com.gitlab.sszuev.flashcards.services.TranslationService
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.Clock

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.api.controllers.TranslationController")

suspend fun ApplicationCall.fetchTranslation(
    service: TranslationService,
    contextConfig: ContextConfig
) {
    execute<FetchTranslationRequest>(TranslationOperation.FETCH_CARD, logger, contextConfig) {
        service.fetchTranslation(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: TranslationOperation,
    logger: ExtLogger,
    contextConfig: ContextConfig,
    noinline exec: suspend TranslationContext.() -> Unit,
) {
    val context = TranslationContext(
        operation = operation,
        timestamp = Clock.System.now(),
        config = contextConfig.toAppConfig(),
    )
    context.fromUserTransport(contextConfig.runConfig.auth)
    execute<R, TranslationContext>(operation, context, logger, exec)
}