package com.gitlab.sszuev.flashcards.stubs

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.*

const val STUB_ERROR_GROUP = "StubErrors"

val stubAudioResource = ResourceEntity(
    resourceId = ResourceId(42.toString()),
    data = ByteArray(42) { 42 },
)

val stubError = AppError(
    field = "the-error-field",
    exception = AssertionError("Test-error"),
    message = "the-error-message",
    code = "StubErrorCode",
    group = STUB_ERROR_GROUP,
)

val stubCard = CardEntity(
    word = "XXX",
    cardId = CardId(42.toString()),
    dictionaryId = DictionaryId(42.toString()),
)

val stubCards = IntRange(1, 3)
    .flatMap { dictionaryId -> IntRange(1, 42).map { cardId -> dictionaryId to cardId } }
    .map {
        stubCard.copy(
            cardId = CardId(it.second.toString()),
            dictionaryId = DictionaryId(it.first.toString()),
            word = "XXX-${it.first}-${it.second}"
        )
    }

val stubLearnCardDetails = CardLearn(
    cardId = CardId(42.toString()),
    details = mapOf("stage-a" to 42, "stage-b" to 5, "stage-c" to 4)
)

fun stubErrorForCode(case: AppStub): AppError {
    return AppError(
        field = "field::$case",
        message = "the-error-message-for-$case",
        code = case.name,
        group = STUB_ERROR_GROUP,
    )
}