package com.gitlab.sszuev.flashcards.common

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

fun wrongDictionaryLanguageFamilies(
    operation: String,
    dictionaryIds: Collection<DictionaryId>,
) = dbError(
    operation = operation,
    fieldName = dictionaryIds.joinToString { it.asString() },
    details = """specified dictionaries belong to different language families, ids="${dictionaryIds.map { it.asString() }}""""
)

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

fun wrongUserUUIDDbError(
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