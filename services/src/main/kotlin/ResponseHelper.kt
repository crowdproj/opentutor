package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus

fun CardContext.errorResponse(
    buildError: () -> AppError,
    error: (AppError) -> AppError = { it -> it }
) = apply {
    status = AppStatus.FAIL
    errors.add(error(buildError()))
}

fun CardContext.successResponse(context: CardContext.() -> Unit) = apply(context).apply { status = AppStatus.OK }