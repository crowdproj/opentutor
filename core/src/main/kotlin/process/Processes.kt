package com.gitlab.sszuev.flashcards.core.process

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.ResourceGet

internal fun ResourceGet.toFieldName(): String {
    return toString()
}

internal fun runError(
    operation: CardOperation,
    fieldName: String = "",
    description: String = "",
    exception: Throwable? = null,
) = AppError(
    code = "run::$operation",
    field = fieldName,
    group = "run",
    message = if (description.isBlank()) "" else "Error while $operation: $description",
    exception = exception
)

internal fun CardContext.fail(error: AppError) {
    this.status = AppStatus.FAIL
    this.errors.add(error)
}