package com.gitlab.sszuev.flashcards.core.process

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation

fun ChainDSL<CardContext>.processFindUser(operation: CardOperation) = worker {
    this.name = "process get-user"
    process {
        val uid = this.normalizedRequestUserUid
        val res = this.repositories.userRepository(this.workMode).getUser(uid)
        this.contextUserEntity = res.user
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        fail(
            runError(
                operation = operation,
                fieldName = this.normalizedRequestUserUid.toFieldName(),
                description = "exception while get user",
                exception = it,
            )
        )
    }
}