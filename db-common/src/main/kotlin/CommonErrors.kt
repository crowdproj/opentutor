package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.UserUid

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
    uid: UserUid,
) = dbError(operation = operation, fieldName = uid.asString(), details = """user with uid="${uid.asString()}" not found""")

fun wrongUserUUIDDbError(
    operation: String,
    uid: UserUid,
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