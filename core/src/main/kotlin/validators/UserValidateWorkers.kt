package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppOperation

fun <X: AppContext> ChainDSL<X>.validateUserId(operation: AppOperation) = worker {
    this.name = "Test card-id length, operation: $operation"
    test {
        this.normalizedRequestAppAuthId.asString().isBlank()
    }
    process {
        fail(validationError(fieldName = "user-uid", description = "user-uid is required"))
    }
}