package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.domain.*

fun CardContext.fromCardTransport(request: BaseRequest) = when (request) {
    is GetAudioRequest -> fromGetAudioRequest(request)
    is GetCardRequest -> fromGetCardRequest(request)
    is GetAllCardsRequest -> fromGetAllCardsRequest(request)
    is SearchCardsRequest -> fromSearchCardsRequest(request)
    is CreateCardRequest -> fromCreateCardRequest(request)
    is UpdateCardRequest -> fromUpdateCardRequest(request)
    is DeleteCardRequest -> fromDeleteCardRequest(request)
    is LearnCardsRequest -> fromLearnCardRequest(request)
    is ResetCardRequest -> fromResetCardRequest(request)
    else -> throw IllegalArgumentException("Unknown request ${request.javaClass}")
}

fun CardContext.fromGetAudioRequest(request: GetAudioRequest) {
    this.requestId = request.requestId()
    this.requestResourceGet = ResourceGet(word = request.word ?: "", lang = LangId(request.lang ?: ""))
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
}

fun CardContext.fromGetCardRequest(request: GetCardRequest) {
    this.requestId = request.requestId()
    this.requestCardEntityId = toCardId(request.cardId)
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
}

fun CardContext.fromGetAllCardsRequest(request: GetAllCardsRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestDictionaryId = toDictionaryId(request.dictionaryId)
}

fun CardContext.fromSearchCardsRequest(request: SearchCardsRequest) {
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
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntity = request.card?.toCardEntity() ?: CardEntity.EMPTY
}

fun CardContext.fromUpdateCardRequest(request: UpdateCardRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntity = request.card?.toCardEntity() ?: CardEntity.EMPTY
}

fun CardContext.fromDeleteCardRequest(request: DeleteCardRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntityId = toCardId(request.cardId)
}

fun CardContext.fromLearnCardRequest(request: LearnCardsRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardLearnList = request.cards?.map { it.toCardLearn() } ?: emptyList()
}

fun CardContext.fromResetCardRequest(request: ResetCardRequest) {
    this.requestId = request.requestId()
    this.workMode = request.debug.transportToWorkMode()
    this.debugCase = request.debug.transportToStubCase()
    this.requestCardEntityId = toCardId(request.cardId)
}

private fun CardResource.toCardEntity(): CardEntity = CardEntity(
    cardId = toCardId(this.cardId),
    dictionaryId = toDictionaryId(this.dictionaryId),
    word = this.word ?: "",
    partOfSpeech = this.partOfSpeech,
    transcription = this.transcription,
    translations = this.translations ?: emptyList(),
    examples = this.examples ?: emptyList(),
    details = this.details.toDetails(),
    answered = this.answered,
    sound = this.sound?.takeIf { it.isNotBlank() }?.let { ResourceId(it) }?: ResourceId.NONE
)

private fun String.toStage(): Stage? {
    val str = this.replace("-", "_")
    return Stage.values().firstOrNull { it.name.equals(other = str, ignoreCase = true) }
}

private fun LearnResource?.toCardLearn(): CardLearn = CardLearn(
    cardId = toCardId(this?.cardId),
    details = this?.details.toDetails()
)

private fun Map<String, Long>?.toDetails() = this?.mapNotNull {
    val k = it.key.toStage() ?: return@mapNotNull null
    k to it.value
}?.toMap() ?: emptyMap()
