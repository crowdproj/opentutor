package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.BaseRequest
import com.gitlab.sszuev.flashcards.api.v1.models.BaseResponse
import com.gitlab.sszuev.flashcards.api.v1.models.ErrorResource
import com.gitlab.sszuev.flashcards.api.v1.models.Result
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppRequestId
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation

fun AppContext.fromTransport(request: BaseRequest) = when (this) {
    is CardContext -> fromCardTransport(request)
    is DictionaryContext -> fromDictionaryTransport(request)
    else -> throw IllegalArgumentException("Unsupported request = $request")
}

fun AppContext.toResponse(): BaseResponse = when (this.operation) {
    is CardOperation -> (this as CardContext).toCardResponse()
    is DictionaryOperation -> (this as DictionaryContext).toDictionaryResponse()
    else -> throw IllegalArgumentException("Unknown context = $this")
}

internal fun BaseRequest?.requestId() = this?.requestId?.let { AppRequestId(it) } ?: AppRequestId.NONE

internal fun toCardId(id: String?) = id?.let { CardId(it) } ?: CardId.NONE

internal fun toDictionaryId(id: String?) = id?.let { DictionaryId(it) } ?: DictionaryId.NONE

internal fun List<AppError>.toErrorResourceList(): List<ErrorResource>? = this
    .map { it.toErrorResource() }
    .toList()
    .takeIf { it.isNotEmpty() }

internal fun AppError.toErrorResource() = ErrorResource(
    code = code.takeIf { it.isNotBlank() },
    group = group.takeIf { it.isNotBlank() },
    field = field.takeIf { it.isNotBlank() },
    message = message.takeIf { it.isNotBlank() },
)

internal fun AppStatus.toResponseResult(): Result = if (this == AppStatus.OK) Result.SUCCESS else Result.ERROR

internal fun Id.toResponseId(): String? = this.asString().takeIf { it.isNotBlank() }