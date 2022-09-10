package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun CardContext.toResponse(): BaseResponse = when (val op = this.operation) {
    CardOperation.GET_RESOURCE -> this.toGetAudioResponse()
    CardOperation.GET_ALL_CARDS -> this.toGetAllCardsResponse()
    CardOperation.SEARCH_CARDS -> this.toSearchCardsResponse()
    CardOperation.GET_CARD -> this.toGetCardResponse()
    CardOperation.CREATE_CARD -> this.toCreateCardResponse()
    CardOperation.UPDATE_CARD -> this.toUpdateCardResponse()
    CardOperation.DELETE_CARD -> this.toDeleteCardResponse()
    CardOperation.LEARN_CARDS -> this.toLearnCardResponse()
    CardOperation.RESET_CARD -> this.toResetCardResponse()
    else -> throw IllegalArgumentException("Not supported operation $op.")
}

fun CardContext.toGetAudioResponse() = GetAudioResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    resource = this.responseResourceEntity.data,
)

fun CardContext.toGetCardResponse() = GetCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    card = this.responseCardEntity.toCardResource()
)

fun CardContext.toGetAllCardsResponse() = GetAllCardsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    cards = this.responseCardEntityList.mapNotNull { it.toCardResource() }
)

fun CardContext.toSearchCardsResponse() = SearchCardsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    cards = this.responseCardEntityList.mapNotNull { it.toCardResource() }
)

fun CardContext.toCreateCardResponse() = CreateCardResponse(
    card = this.responseCardEntity.toCardResource(),
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toUpdateCardResponse() = UpdateCardResponse(
    card = this.responseCardEntity.toCardResource(),
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toDeleteCardResponse() = DeleteCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toLearnCardResponse() = LearnCardsResponse(
    cards = this.responseCardEntityList.mapNotNull { it.toCardResource() },
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toResetCardResponse() = ResetCardResponse(
    card = this.responseCardEntity.toCardResource(),
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

private fun CardEntity.toCardResource(): CardResource? {
    if (this == CardEntity.EMPTY) {
        return null
    }
    return CardResource(
        cardId = cardId.takeIf { it != CardId.NONE }?.asString(),
        dictionaryId = dictionaryId.takeIf { it != DictionaryId.NONE }?.asString(),
        word = word,
        partOfSpeech = partOfSpeech,
        transcription = transcription,
        translations = translations,
        examples = examples,
        details = details.mapKeys { it.key.name },
        answered = answered,
    )
}