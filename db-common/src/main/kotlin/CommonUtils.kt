package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun DictionaryId.asDbId(): Long {
    if (asString().matches("\\d+".toRegex())) {
        return asString().toLong()
    }
    throw IllegalArgumentException("Wrong dictionary id ${asString()}.")
}

fun notFoundDbError(
    operation: String,
    fieldName: String = "",
) = dbError(operation = operation, fieldName = fieldName, details = """dictionary with id="$fieldName" not found""")

fun dbError(
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