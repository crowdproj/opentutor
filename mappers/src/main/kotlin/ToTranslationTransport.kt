package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.TranslationContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.FetchTranslationResponse
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation

fun TranslationContext.toTranslationResponse(): BaseResponse = when (val op = this.operation) {
    TranslationOperation.FETCH_CARD -> this.toFetchTranslationResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}

fun TranslationContext.toFetchTranslationResponse() = FetchTranslationResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    card = this.responseCardEntity.toCardResource()
)
