package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.common.AppMode
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.*

fun CardContext.fromTransport(request: BaseRequest) = when (request) {
    is GetAudioRequest -> fromGetGetAudioRequest(request)
    is GetCardRequest -> fromGetCardRequest(request)
    is GetCardsRequest -> fromGetCardsRequest(request)
    is CreateCardRequest -> fromCreateCardRequest(request)
    is UpdateCardRequest -> fromUpdateCardRequest(request)
    is DeleteCardRequest -> fromDeleteCardRequest(request)
    is LearnCardRequest -> fromLearnCardRequest(request)
    is ResetCardRequest -> fromResetCardRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass}")
}

fun CardContext.fromGetGetAudioRequest(request: GetAudioRequest) {
    this.operation = CardOperation.GET_RESOURCE
    this.requestId = request.requestId()
    this.requestResourceGet = ResourceGet(word = request.word?:"", lang = LangId(request.lang?:""))
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
}

fun CardContext.fromGetCardRequest(request: GetCardRequest) {
    this.operation = CardOperation.GET_CARD
    this.requestId = request.requestId()
    this.requestCardEntityId = toCardId(request.cardId)
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
}

fun CardContext.fromGetCardsRequest(request: GetCardsRequest) {
    this.operation = CardOperation.SEARCH_CARDS
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

fun CardContext.fromCreateCardRequest(request: CreateCardRequest) {
    this.operation = CardOperation.CREATE_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntity = request.card.toCardEntity()
}

fun CardContext.fromUpdateCardRequest(request: UpdateCardRequest) {
    this.operation = CardOperation.UPDATE_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntity = request.card.toCardEntity()
}

fun CardContext.fromDeleteCardRequest(request: DeleteCardRequest) {
    this.operation = CardOperation.DELETE_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntityId = toCardId(request.cardId)
}

fun CardContext.fromLearnCardRequest(request: LearnCardRequest) {
    this.operation = CardOperation.LEARN_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardLearnList = request.cards?.map { it.toCardLearn() } ?: emptyList()
}

fun CardContext.fromResetCardRequest(request: ResetCardRequest) {
    this.operation = CardOperation.RESET_CARD
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntityId = toCardId(request.cardId)
}

private fun BaseRequest?.requestId() = this?.requestId?.let { AppRequestId(it) } ?: AppRequestId.NONE

private fun toLangId(id: String?) = id?.let { LangId(it) } ?: LangId.NONE

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

private fun DebugResource?.transportToWorkMode(): AppMode = when (this?.mode) {
    RunMode.PROD -> AppMode.PROD
    RunMode.TEST -> AppMode.TEST
    RunMode.STUB -> AppMode.STUB
    null -> AppMode.PROD
}

private fun DebugResource?.transportToStubCase(): AppStub = when (this?.stub) {
    DebugStub.SUCCESS -> AppStub.SUCCESS
    DebugStub.ERROR_UNKNOWN -> AppStub.UNKNOWN_ERROR
    DebugStub.ERROR_UNEXPECTED_FIELD -> AppStub.ERROR_UNEXPECTED_FIELD
    DebugStub.ERROR_WRONG_CARD_ID -> AppStub.ERROR_WRONG_CARD_ID
    DebugStub.ERROR_CARD_WRONG_WORD -> AppStub.ERROR_CARD_WRONG_WORD
    DebugStub.ERROR_CARD_WRONG_TRANSCRIPTION -> AppStub.ERROR_CARD_WRONG_TRANSCRIPTION
    DebugStub.ERROR_CARD_WRONG_TRANSLATION -> AppStub.ERROR_CARD_WRONG_TRANSLATION
    DebugStub.ERROR_CARD_WRONG_EXAMPLES -> AppStub.ERROR_CARD_WRONG_EXAMPLES
    DebugStub.ERROR_CARD_WRONG_PART_OF_SPEECH -> AppStub.ERROR_CARD_WRONG_PART_OF_SPEECH
    DebugStub.ERROR_CARD_WRONG_DETAILS -> AppStub.ERROR_CARD_WRONG_DETAILS
    DebugStub.ERROR_CARD_WRONG_AUDIO_RESOURCE -> AppStub.ERROR_CARD_WRONG_AUDIO_RESOURCE
    DebugStub.ERROR_AUDIO_RESOURCE_WRONG_RESOURCE_ID -> AppStub.ERROR_AUDIO_RESOURCE_WRONG_RESOURCE_ID
    DebugStub.ERROR_AUDIO_RESOURCE_SERVER_ERROR -> AppStub.ERROR_AUDIO_RESOURCE_SERVER_ERROR
    DebugStub.ERROR_AUDIO_RESOURCE_NOT_FOUND -> AppStub.ERROR_AUDIO_RESOURCE_NOT_FOUND
    DebugStub.ERROR_CARDS_FILTER_WRONG_LENGTH -> AppStub.ERROR_CARDS_FILTER_WRONG_LENGTH
    DebugStub.ERROR_CARDS_FILTER_WRONG_DICTIONARY_ID -> AppStub.ERROR_CARDS_FILTER_WRONG_DICTIONARY_ID
    DebugStub.ERROR_LEARN_CARD_WRONG_CARD_ID -> AppStub.ERROR_LEARN_CARD_WRONG_CARD_ID
    DebugStub.ERROR_LEARN_CARD_WRONG_STAGES -> AppStub.ERROR_LEARN_CARD_WRONG_STAGES
    DebugStub.ERROR_LEARN_CARD_WRONG_DETAILS -> AppStub.ERROR_LEARN_CARD_WRONG_DETAILS
    null -> AppStub.NONE
}