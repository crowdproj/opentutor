package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun CardContext.toResponse(): BaseResponse = when (val op = this.operation) {
    CardOperation.GET_RESOURCE -> this.toGetAudioResponse()
    CardOperation.SEARCH_CARDS -> this.toGetCardsResponse()
    CardOperation.GET_CARD -> this.toGetCardResponse()
    CardOperation.CREATE_CARD -> this.toCreateCardResponse()
    CardOperation.UPDATE_CARD -> this.toUpdateCardResponse()
    CardOperation.DELETE_CARD -> this.toDeleteCardResponse()
    CardOperation.LEARN_CARD -> this.toLearnCardResponse()
    CardOperation.RESET_CARD -> this.toResetCardResponse()
    CardOperation.NONE -> throw IllegalArgumentException("Not supported operation $op.")
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

fun CardContext.toGetCardsResponse() = GetCardsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    cards = this.responseCardEntityList.mapNotNull { it.toCardResource() }
)

fun CardContext.toCreateCardResponse() = CreateCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toUpdateCardResponse() = UpdateCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toDeleteCardResponse() = DeleteCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toLearnCardResponse() = LearnCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun CardContext.toResetCardResponse() = ResetCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

private fun CardEntity.toCardResource(): CardResource? {
    if (this == CardEntity.DUMMY) {
        return null
    }
    return CardResource(
        cardId = cardId.takeIf { it != CardId.NONE }?.asString(),
        dictionaryId = dictionaryId.takeIf { it != DictionaryId.NONE }?.asString(),
        word = word
    )
}

private fun List<AppError>.toErrorResourceList(): List<ErrorResource>? = this
    .map { it.toErrorResource() }
    .toList()
    .takeIf { it.isNotEmpty() }

private fun AppError.toErrorResource() = ErrorResource(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() },
)

private fun AppStatus.toResponseResult(): Result = if (this == AppStatus.OK) Result.SUCCESS else Result.ERROR

private fun Id.toResponseId(): String? = this.asString().takeIf { it.isNotBlank() }