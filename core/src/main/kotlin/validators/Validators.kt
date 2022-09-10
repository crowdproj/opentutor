package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus

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