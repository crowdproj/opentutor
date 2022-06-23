package com.gitlab.sszuev.flashcards.stubs

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

val stubError = AppError(
    field = "the-error-field",
    exception = AssertionError("Test-error"),
    message = "the-error-message",
    code = "StubErrorCode",
    group = "StubErrors",
)

val stubCard = CardEntity(
    word = "XXX",
    cardId = CardId(42.toString()),
    dictionaryId = DictionaryId(42.toString()),
)

val stubLearnCardDetails = CardLearn(
    cardId = CardId(42.toString()),
    details = mapOf("stage-a" to 42, "stage-b" to 5, "stage-c" to 4)
)

fun stubErrorForCode(case: AppStub): AppError {
    return stubError.copy(
        code = case.name
    )
}

fun toStatus(case: AppStub): AppStatus {
    return when (case) {
        AppStub.SUCCESS -> {
            AppStatus.OK
        }
        AppStub.NONE -> {
            AppStatus.INIT
        }
        else -> {
            AppStatus.FAIL
        }
    }
}