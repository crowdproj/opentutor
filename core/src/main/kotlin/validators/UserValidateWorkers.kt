package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.domain.CardOperation

fun ChainDSL<CardContext>.validateUserId(operation: CardOperation) = worker {
    this.name = "Test card-id length, operation: $operation"
    test {
        this.normalizedRequestAppAuthId.asString().isBlank()
    }
    process {
        fail(validationError(fieldName = "user-uid", description = "user-uid is required"))
    }
}