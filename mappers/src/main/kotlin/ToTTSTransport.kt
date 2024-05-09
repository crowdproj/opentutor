package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioResponse
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation

fun TTSContext.toTTSResponse(): BaseResponse = when (val op = this.operation) {
    TTSOperation.GET_RESOURCE -> this.toGetAudioResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}


fun TTSContext.toGetAudioResponse() = GetAudioResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    resource = this.responseTTSResourceEntity.data,
)
