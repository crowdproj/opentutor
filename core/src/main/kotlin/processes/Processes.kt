package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppOperation
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet

internal fun TTSResourceGet.toFieldName(): String {
    return toString()
}

internal fun Id.toFieldName(): String {
    return asString()
}

internal fun runError(
    operation: AppOperation,
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

internal fun AppContext.handleThrowable(operation: AppOperation, ex: Throwable) {
    fail(
        runError(
            operation = operation,
            description = "exception",
            exception = ex,
        )
    )
}

internal fun AppContext.fail(error: AppError) {
    this.status = AppStatus.FAIL
    this.errors.add(error)
}
