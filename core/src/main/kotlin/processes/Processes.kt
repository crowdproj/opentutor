package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppOperation
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet

internal fun TTSResourceGet.toFieldName(): String {
    return toString()
}

internal fun Id.toFieldName(): String {
    return asString()
}

fun forbiddenEntityDataError(
    operation: String,
    entityId: Id,
    userId: AppUserId,
) = dataError(
    operation = operation,
    fieldName = entityId.asString(),
    details = when (entityId) {
        is DictionaryId -> "access denied: the dictionary (id=${entityId.asString()}) is not owned by the used (id=${userId.asString()})"
        is CardId -> "access denied: the card (id=${entityId.asString()}) is not owned by the the used (id=${userId.asString()})"
        else -> throw IllegalArgumentException()
    },
)

fun noDictionaryFoundDataError(
    operation: String,
    id: DictionaryId,
) = dataError(
    operation = operation,
    fieldName = id.asString(),
    details = """dictionary with id="${id.toFieldName()}" not found"""
)

fun noCardFoundDataError(
    operation: String,
    id: CardId,
) = dataError(
    operation = operation,
    fieldName = id.toFieldName(),
    details = """card with id="${id.asString()}" not found"""
)

fun dataError(
    operation: String,
    fieldName: String = "",
    details: String = "",
    exception: Throwable? = null,
) = AppError(
    code = "database::$operation",
    field = fieldName,
    group = "database",
    message = if (details.isBlank()) "Error while $operation" else "Error while $operation: $details",
    exception = exception
)

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
