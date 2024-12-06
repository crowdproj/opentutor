package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.FetchTranslationRequest
import com.gitlab.sszuev.flashcards.model.domain.LangId

fun TranslationContext.fromTranslationTransport(request: BaseRequest) = when (request) {
    is FetchTranslationRequest -> fromFetchTranslationRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass}")
}

fun TranslationContext.fromFetchTranslationRequest(request: FetchTranslationRequest) {
    this.requestId = request.requestId()
    this.requestSourceLang = LangId(request.sourceLang ?: "")
    this.requestTargetLang = LangId(request.targetLang ?: "")
    this.requestWord = request.word ?: ""
}