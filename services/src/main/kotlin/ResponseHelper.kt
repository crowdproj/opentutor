package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.AppContext
import com.gitlab.sszuev.flashcards.model.common.Error
import com.gitlab.sszuev.flashcards.model.common.Status

fun AppContext.errorResponse(
    buildError: () -> Error,
    error: (Error) -> Error = { it -> it }
) = apply {
    status = Status.FAIL
    errors.add(error(buildError()))
}

fun AppContext.successResponse(context: AppContext.() -> Unit) = apply(context).apply { status = Status.OK }