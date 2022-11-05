package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId

/**
 * Tests word.
 * See [wiki: Longest words](https://en.wikipedia.org/wiki/Longest_words).
 * @param [txt] to test
 * @return [Boolean]
 */
internal fun isCorrectWrong(txt: String): Boolean {
    return txt.isNotBlank() && txt.length <= 256
}

/**
 * Example of [langTag]: `EN`, `DE`.
 */
internal fun isCorrectLangId(langTag: String): Boolean {
    return langTag.length in 2..3 && langTag.matches("[A-Za-z]+".toRegex())
}

internal fun validationError(
    fieldName: String,
    description: String = ""
) = AppError(
    code = "validation-$fieldName",
    field = fieldName,
    group = "validators",
    message = if (description.isBlank()) "" else "validation error for $fieldName: $description"
)

internal fun AppContext.fail(error: AppError) {
    this.status = AppStatus.FAIL
    this.errors.add(error)
}

internal fun <Context: AppContext> ChainDSL<Context>.validateDictionaryId(getDictionaryId: (Context) -> DictionaryId) = worker {
    validateId("dictionary-id") { getDictionaryId(it) }
}

internal fun <Context: AppContext> ChainDSL<Context>.validateId(
    fieldName: String,
    getId: (Context) -> Id
) = chain {
    name = "validate ids: fieldName=$fieldName"
    validateIdIsNotBlank(workerName = "Test $fieldName length", fieldName = fieldName, getId = getId)
    validateIdMatchPattern(workerName = "Test $fieldName pattern", fieldName = fieldName, getId = getId)
}

internal fun <Context: AppContext> ChainDSL<Context>.validateIdIsNotBlank(
    workerName: String,
    fieldName: String,
    getId: (Context) -> Id
) = worker {
    this.name = workerName
    test {
        isIdBlank(getId(this))
    }
    process {
        fail(validationError(fieldName = fieldName, description = "it is blank"))
    }
}

internal fun <Context: AppContext> ChainDSL<Context>.validateIdMatchPattern(
    workerName: String,
    fieldName: String,
    getId: (Context) -> Id
) = worker {
    this.name = workerName
    test {
        val id = getId(this)
        !isIdBlank(id) && isIdWrong(id)
    }
    process {
        fail(validationError(fieldName = fieldName, description = "must be integer number"))
    }
}

internal fun isIdBlank(id: Id): Boolean {
    return id.asString().isBlank()
}

internal fun isIdWrong(id: Id): Boolean {
    return !id.asString().matches(Regex("\\d+"))
}
