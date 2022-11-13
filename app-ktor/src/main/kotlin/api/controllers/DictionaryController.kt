package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.api.services.DictionaryService
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.config.RunConfig
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.logslib.logger
import com.gitlab.sszuev.flashcards.mappers.v1.fromUserTransport
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import io.ktor.server.application.*
import kotlinx.datetime.Clock

private val logger: ExtLogger = logger("com.gitlab.sszuev.flashcards.api.controllers.DictionaryControllerKt")

suspend fun ApplicationCall.getAllDictionaries(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    runConf: RunConfig
) {
    execute<GetAllDictionariesRequest>(
        DictionaryOperation.GET_ALL_DICTIONARIES,
        repositories,
        logger,
        runConf
    ) {
        service.getAllDictionaries(this)
    }
}

suspend fun ApplicationCall.createDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    runConf: RunConfig
) {
    execute<CreateDictionaryRequest>(
        DictionaryOperation.CREATE_DICTIONARY,
        repositories,
        logger,
        runConf
    ) {
        service.createDictionary(this)
    }
}

suspend fun ApplicationCall.deleteDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    runConf: RunConfig
) {
    execute<DeleteDictionaryRequest>(
        DictionaryOperation.DELETE_DICTIONARY,
        repositories,
        logger,
        runConf
    ) {
        service.deleteDictionary(this)
    }
}

suspend fun ApplicationCall.downloadDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    runConf: RunConfig
) {
    execute<DownloadDictionaryRequest>(
        DictionaryOperation.DOWNLOAD_DICTIONARY,
        repositories,
        logger,
        runConf
    ) {
        service.downloadDictionary(this)
    }
}

suspend fun ApplicationCall.uploadDictionary(
    service: DictionaryService,
    repositories: DictionaryRepositories,
    runConf: RunConfig
) {
    execute<UploadDictionaryRequest>(
        DictionaryOperation.UPLOAD_DICTIONARY,
        repositories,
        logger,
        runConf
    ) {
        service.uploadDictionary(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: DictionaryOperation,
    repositories: DictionaryRepositories,
    logger: ExtLogger,
    runConf: RunConfig,
    noinline exec: suspend DictionaryContext.() -> Unit,
) {
    val context = DictionaryContext(
        operation = operation,
        timestamp = Clock.System.now(),
        repositories = repositories
    )
    context.fromUserTransport(runConf.auth)
    execute<R, DictionaryContext>(operation, context, logger, exec)
}