package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.common.Mode
import com.gitlab.sszuev.flashcards.model.common.Operation
import com.gitlab.sszuev.flashcards.model.common.RequestId
import com.gitlab.sszuev.flashcards.model.common.Stub
import com.gitlab.sszuev.flashcards.model.domain.*

fun AppContext.fromTransport(request: BaseRequest) = when (request) {
    is GetCardRequest -> fromGetCardRequest(request)
    is GetCardsRequest -> fromGetCardsRequest(request)
    is CreateCardRequest -> fromCreateCardRequest(request)
    is UpdateCardRequest -> fromUpdateCardRequest(request)
    is DeleteCardRequest -> fromDeleteCardRequest(request)
    is LearnCardRequest -> fromLearnCardRequest(request)
    is ResetCardRequest -> fromResetCardRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass}")
}

fun AppContext.fromGetCardRequest(request: GetCardRequest) {
    this.operation = Operation.GET_CARD
    this.requestId = request.requestId()
    this.requestCardEntityId = toCardId(request.cardId)
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
}

fun AppContext.fromGetCardsRequest(request: GetCardsRequest) {
    this.operation = Operation.GET_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardFilter = CardFilter(
        dictionaryIds = request.dictionaryIds?.map { toDictionaryId(it) } ?: mutableListOf(),
        length = request.length ?: 0,
        random = request.random ?: false,
        withUnknown = request.unknown ?: false,
    )
}

fun AppContext.fromCreateCardRequest(request: CreateCardRequest) {
    this.operation = Operation.CREATE_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntity = request.card.toCardEntity()
}

fun AppContext.fromUpdateCardRequest(request: UpdateCardRequest) {
    this.operation = Operation.UPDATE_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntity = request.card.toCardEntity()
}

fun AppContext.fromDeleteCardRequest(request: DeleteCardRequest) {
    this.operation = Operation.DELETE_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntityId = toCardId(request.cardId)
}

fun AppContext.fromLearnCardRequest(request: LearnCardRequest) {
    this.operation = Operation.LEARN_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardLearnList = request.cards?.map { it.toCardLearn() } ?: emptyList()
}

fun AppContext.fromResetCardRequest(request: ResetCardRequest) {
    this.operation = Operation.RESET_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntityId = toCardId(request.cardId)
}

private fun BaseRequest?.requestId() = this?.requestId?.let { RequestId(it) } ?: RequestId.NONE

private fun toCardId(id: String?) = id?.let { CardId(it) } ?: CardId.NONE

private fun toDictionaryId(id: String?) = id?.let { DictionaryId(it) } ?: DictionaryId.NONE

private fun CardResource?.toCardEntity(): CardEntity = CardEntity(
    cardId = toCardId(this?.cardId),
    dictionaryId = toDictionaryId(this?.dictionaryId),
    word = this?.word ?: ""
)

private fun CardUpdateResource?.toCardLearn(): CardLearn = CardLearn(
    cardId = toCardId(this?.cardId),
    details = this?.details ?: emptyMap()
)

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