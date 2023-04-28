package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CreateCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.DeleteCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAllCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetAudioRequest
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.LearnResource
import com.gitlab.sszuev.flashcards.api.v1.models.ResetCardRequest
import com.gitlab.sszuev.flashcards.api.v1.models.SearchCardsRequest
import com.gitlab.sszuev.flashcards.api.v1.models.UpdateCardRequest
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

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
    this.requestTTSResourceGet = TTSResourceGet(word = request.word ?: "", lang = LangId(request.lang ?: ""))
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

private fun CardResource.toCardEntity(): CardEntity = CardEntity( // TODO: change to new model
    cardId = toCardId(this.cardId),
    dictionaryId = toDictionaryId(this.dictionaryId),
    words = listOf(
        CardWordEntity(
            word = this.word ?: "",
            partOfSpeech = this.partOfSpeech,
            transcription = this.transcription,
            translations = this.translations ?: emptyList(),
            examples = this.examples?.map { CardWordExampleEntity(it) } ?: emptyList(),
            sound = this.sound?.takeIf { it.isNotBlank() }?.let { TTSResourceId(it) } ?: TTSResourceId.NONE
        ),
    ),
    stats = this.details.toStats(),
    answered = this.answered,
)

private fun String.toStage(): Stage? {
    val str = this.replace("-", "_")
    return Stage.values().firstOrNull { it.name.equals(other = str, ignoreCase = true) }
}

private fun LearnResource?.toCardLearn(): CardLearn = CardLearn(
    cardId = toCardId(this?.cardId),
    details = this?.details.toStats()
)

private fun Map<String, Long>?.toStats() = this?.mapNotNull {
    val k = it.key.toStage() ?: return@mapNotNull null
    k to it.value
}?.toMap() ?: emptyMap()
