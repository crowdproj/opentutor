package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.Error
import com.gitlab.sszuev.flashcards.model.common.Status
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun AppContext.toGetCardResponse() = GetCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    card = this.responseCardEntity.toCardResource()
)

fun AppContext.toGetCardsResponse() = GetCardsResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
    cards = this.responseCardEntityList.map { it.toCardResource() }
)

fun AppContext.toCreateCardResponse() = CreateCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun AppContext.toUpdateCardResponse() = UpdateCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun AppContext.toDeleteCardResponse() = DeleteCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun AppContext.toLearnCardResponse() = LearnCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

fun AppContext.toResetCardResponse() = ResetCardResponse(
    requestId = this.requestId.toResponseId(),
    result = this.status.toResponseResult(),
    errors = this.errors.toErrorResourceList(),
)

private fun CardEntity.toCardResource(): CardResource = CardResource(
    cardId = cardId.takeIf { it != CardId.NONE }?.asString(),
    dictionaryId = dictionaryId.takeIf { it != DictionaryId.NONE }?.asString(),
    word = word
)

private fun List<Error>.toErrorResourceList(): List<ErrorResource>? = this
    .map { it.toErrorResource() }
    .toList()
    .takeIf { it.isNotEmpty() }

private fun Error.toErrorResource() = ErrorResource(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() },
)

private fun Status.toResponseResult(): Result = if (this == Status.OK) Result.SUCCESS else Result.ERROR

private fun Id.toResponseId(): String? = this.asString().takeIf { it.isNotBlank() }