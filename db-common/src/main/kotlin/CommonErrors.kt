package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun forbiddenEntityDbError(
    operation: String,
    entityId: Id,
    userId: AppUserId,
): AppError {
    return dbError(
        operation = operation,
        fieldName = entityId.asString(),
        details = when (entityId) {
            is DictionaryId -> "access denied: the dictionary (id=${entityId.asString()}) is not owned by the used (id=${userId.asString()})"
            is CardId -> "access denied: the card (id=${entityId.asString()}) is not owned by the the used (id=${userId.asString()})"
            else -> throw IllegalArgumentException()
        },
    )
}

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

fun wrongResourceDbError(exception: Throwable) = dbError(
    operation = "uploadDictionary",
    details = """can't parse dictionary from byte-array""",
    exception = exception,
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