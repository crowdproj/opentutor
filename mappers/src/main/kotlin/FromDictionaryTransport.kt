package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllDictionariesRequest

fun DictionaryContext.fromTransportToRequest(request: BaseRequest) = when (request) {
    is GetAllDictionariesRequest -> fromGetAllDictionariesRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass.simpleName}")
}

fun DictionaryContext.fromGetAllDictionariesRequest(request: GetAllDictionariesRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
}