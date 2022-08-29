package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun noDictionaryFoundDbError(
    operation: String,
    id: DictionaryId,
) = dbError(
    operation = operation,
    fieldName = id.asString(),
    details = """dictionary with id="${id.asString()}" not found"""
)

fun noCardFoundDbError(
    operation: String,
    id: CardId,
) = dbError(operation = operation, fieldName = id.asString(), details = """card with id="${id.asString()}" not found""")

fun noUserFoundDbError(
    operation: String,
    uid: String,
) = dbError(operation = operation, fieldName = uid, details = """user with uid="$uid" not found""")

fun wrongUserUUIDDbError(
    operation: String,
    uid: String,
) = dbError(
    operation = operation,
    fieldName = uid,
    details = "wrong uuid=<$uid.>",
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