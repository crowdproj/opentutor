package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.TranslationContext
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
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation
import com.gitlab.sszuev.flashcards.model.domain.TranslationOperation

fun AppContext.fromTransport(request: BaseRequest) = when (this) {
    is CardContext -> fromCardTransport(request)
    is DictionaryContext -> fromDictionaryTransport(request)
    is TTSContext -> fromTTSTransport(request)
    is SettingsContext -> fromSettingsTransport(request)
    is TranslationContext -> fromTranslationTransport(request)
    else -> throw IllegalArgumentException("Unsupported request = $request")
}

fun AppContext.toResponse(): BaseResponse = when (this.operation) {
    is CardOperation -> (this as CardContext).toCardResponse()
    is DictionaryOperation -> (this as DictionaryContext).toDictionaryResponse()
    is TTSOperation -> (this as TTSContext).toTTSResponse()
    is SettingsOperation -> (this as SettingsContext).toSettingsResponse()
    is TranslationOperation -> (this as TranslationContext).toTranslationResponse()
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