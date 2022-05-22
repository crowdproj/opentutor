package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.ErrorResource
import com.gitlab.sszuev.flashcards.api.v1.models.GetCardResponse
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.model.common.Error
import com.gitlab.sszuev.flashcards.model.common.Status
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun AppContext.toGetCardResponse() = GetCardResponse(
    requestId = this.requestId.asString().takeIf { it.isNotBlank() },
    result = if (status == Status.OK) Result.SUCCESS else Result.ERROR,
    errors = errors.toErrorResourceList(),
    card = responseEntity.toCardResource()
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