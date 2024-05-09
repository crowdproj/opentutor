package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioRequest
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet

fun TTSContext.fromTTSTransport(request: BaseRequest) = when (request) {
    is GetAudioRequest -> fromGetAudioRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass}")
}

fun TTSContext.fromGetAudioRequest(request: GetAudioRequest) {
    this.requestId = request.requestId()
    this.requestTTSResourceGet = TTSResourceGet(word = request.word ?: "", lang = LangId(request.lang ?: ""))
}