package com.gitlab.sszuev.flashcards.repositories

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppError

fun noUserFoundDbError(
    operation: String,
    uid: AppAuthId,
) = dbError(
    operation = operation,
    fieldName = uid.asString(),
    details = """user with uid="${uid.asString()}" not found"""
)

fun wrongUserUuidDbError(
    operation: String,
    uid: AppAuthId,
) = dbError(
    operation = operation,
    fieldName = uid.asString(),
    details = """wrong uuid="${uid.asString()}"""",
)

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