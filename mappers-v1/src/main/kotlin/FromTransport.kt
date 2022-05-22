package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.common.Mode
import com.gitlab.sszuev.flashcards.model.common.Operation
import com.gitlab.sszuev.flashcards.model.common.RequestId
import com.gitlab.sszuev.flashcards.model.common.Stub
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId

fun AppContext.fromTransport(request: BaseRequest) = when (request) {
    is GetCardRequest -> fromGetCardRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass}")
}

fun AppContext.fromGetCardRequest(request: GetCardRequest) {
    operation = Operation.GET_CARD
    requestId = request.requestId()
    requestEntity = toCardEntity(request.cardId)
    workMode = request.debug.transportToWorkMode()
    debugCase = request.debug.transportToStubCase()
}

private fun BaseRequest?.requestId() = this?.requestId?.let { RequestId(it) } ?: RequestId.NONE

private fun toCardEntity(id: String?) = CardEntity(cardId = toCardId(id))

private fun toCardId(id: String?) = id?.let { CardId(it) } ?: CardId.NONE

private fun DebugResource?.transportToWorkMode(): Mode = when (this?.mode) {
    RunMode.PROD -> Mode.PROD
    RunMode.TEST -> Mode.TEST
    RunMode.STUB -> Mode.STUB
    null -> Mode.PROD
}

private fun DebugResource?.transportToStubCase(): Stub = when (this?.stub) {
    DebugStub.SUCCESS -> Stub.SUCCESS
    DebugStub.ERROR -> Stub.ERROR
    null -> Stub.NONE
}