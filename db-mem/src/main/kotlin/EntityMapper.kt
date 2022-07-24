package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

internal fun Card.toEntity() = CardEntity(
    cardId = id.asCardId(),
    dictionaryId = dictionaryId.asDictionaryId(),
    word = text,
    transcription = transcription,
    translations = translations.map { it.text }.toList(),
    examples = examples.map { it.text }.toList(),
    partOfSpeech = partOfSpeech,
    details = details,
    answered = answered,
)

private fun Long.asDictionaryId() = DictionaryId(toString())

private fun Long.asCardId() = CardId(toString())

internal fun DictionaryId.asDbId(): Long {
    if (asString().matches("\\d+".toRegex())) {
        return asString().toLong()
    }
    throw IllegalArgumentException("Wrong dictionary id ${asString()}.")
}

internal fun notFoundDbError(
    operation: String,
    fieldName: String = "",
) = dbError(operation = operation, fieldName = fieldName, description = """dictionary with id="$fieldName" not found""")

internal fun dbError(
    operation: String,
    fieldName: String = "",
    description: String = "",
    exception: Throwable? = null,
) = AppError(
    code = "database::$operation",
    field = fieldName,
    group = "database",
    message = if (description.isBlank()) "" else "Error while $operation: $description",
    exception = exception
)