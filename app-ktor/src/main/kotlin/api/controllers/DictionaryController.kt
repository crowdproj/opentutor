package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.api.services.DictionaryService
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CreateDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DownloadDictionaryRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UploadDictionaryRequest
import com.gitlab.sszuev.flashcards.config.ContextConfig
import com.gitlab.sszuev.flashcards.config.toAppConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.Clock

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.api.controllers.DictionaryControllerKt")

suspend fun ApplicationCall.getAllDictionaries(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    contextConfig: ContextConfig
) {
    execute<GetAllDictionariesRequest>(
        operation = DictionaryOperation.GET_ALL_DICTIONARIES,
        repositories = repositories,
        logger = logger,
        contextConfig = contextConfig,
    ) {
        service.getAllDictionaries(this)
    }
}

suspend fun ApplicationCall.createDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    contextConfig: ContextConfig
) {
    execute<CreateDictionaryRequest>(
        DictionaryOperation.CREATE_DICTIONARY,
        repositories,
        logger,
        contextConfig
    ) {
        service.createDictionary(this)
    }
}

suspend fun ApplicationCall.deleteDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    contextConfig: ContextConfig
) {
    execute<DeleteDictionaryRequest>(
        DictionaryOperation.DELETE_DICTIONARY,
        repositories,
        logger,
        contextConfig
    ) {
        service.deleteDictionary(this)
    }
}

suspend fun ApplicationCall.downloadDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    contextConfig: ContextConfig
) {
    execute<DownloadDictionaryRequest>(
        DictionaryOperation.DOWNLOAD_DICTIONARY,
        repositories,
        logger,
        contextConfig
    ) {
        service.downloadDictionary(this)
    }
}

suspend fun ApplicationCall.uploadDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    contextConfig: ContextConfig
) {
    execute<UploadDictionaryRequest>(
        DictionaryOperation.UPLOAD_DICTIONARY,
        repositories,
        logger,
        contextConfig
    ) {
        service.uploadDictionary(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: DictionaryOperation,
    repositories: DictionaryRepositories,
    logger: ExtLogger,
    contextConfig: ContextConfig,
    noinline exec: suspend DictionaryContext.() -> Unit,
) {
    val context = DictionaryContext(
        operation = operation,
        timestamp = Clock.System.now(),
        repositories = repositories,
        config = contextConfig.toAppConfig(),
    )
    context.fromUserTransport(contextConfig.runConfig.auth)
    execute<R, DictionaryContext>(operation, context, logger, exec)
}