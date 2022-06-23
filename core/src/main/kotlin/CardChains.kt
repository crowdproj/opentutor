package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStatus

internal fun CardContext.fail(error: AppError) {
    this.status = AppStatus.FAIL
    this.errors.add(error)
}
