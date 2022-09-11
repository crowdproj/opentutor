package com.gitlab.sszuev.flashcards.mappers.v1

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.api.v1.models.*
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.*
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

internal fun DebugResource?.transportToWorkMode(): AppMode = when (this?.mode) {
    RunMode.PROD -> AppMode.PROD
    RunMode.TEST -> AppMode.TEST
    RunMode.STUB -> AppMode.STUB
    null -> AppMode.PROD
}

internal fun DebugResource?.transportToStubCase(): AppStub = when (this?.stub) {
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
    DebugStub.ERROR_WRONG_DICTIONARY_ID -> AppStub.ERROR_WRONG_DICTIONARY_ID
    DebugStub.ERROR_CARDS_WRONG_FILTER_LENGTH -> AppStub.ERROR_CARDS_WRONG_FILTER_LENGTH
    DebugStub.ERROR_LEARN_CARD_WRONG_CARD_ID -> AppStub.ERROR_LEARN_CARD_WRONG_CARD_ID
    DebugStub.ERROR_LEARN_CARD_WRONG_STAGES -> AppStub.ERROR_LEARN_CARD_WRONG_STAGES
    DebugStub.ERROR_LEARN_CARD_WRONG_DETAILS -> AppStub.ERROR_LEARN_CARD_WRONG_DETAILS
    null -> AppStub.NONE
}

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