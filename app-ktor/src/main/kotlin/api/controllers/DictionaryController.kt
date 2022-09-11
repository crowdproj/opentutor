package com.gitlab.sszuev.flashcards.api.controllers

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.DictionaryRepositories
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest
import com.gitlab.sszuev.flashcards.logslib.ExtLogger
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.services.DictionaryService
import io.ktor.server.application.*
import kotlinx.datetime.Clock

suspend fun ApplicationCall.getAllDictionaries(service: DictionaryService, logger: ExtLogger) {
    execute<GetAllDictionariesRequest>(DictionaryOperation.GET_ALL_DICTIONARIES, service.repositories(), logger) {
        service.getAllDictionaries(this)
    }
}

private suspend inline fun <reified R : BaseRequest> ApplicationCall.execute(
    operation: DictionaryOperation,
    repositories: DictionaryRepositories,
    logger: ExtLogger,
    noinline exec: suspend DictionaryContext.() -> Unit,
) {
    val context = DictionaryContext(operation = operation, timestamp = Clock.System.now(), repositories = repositories)
    execute<R, DictionaryContext>(operation, context, logger, exec)
}