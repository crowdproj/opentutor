package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.core.validators.fail
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppOperation
import com.gitlab.sszuev.flashcards.model.common.AppStatus

internal inline fun <reified Context : AppContext> ChainDSL<Context>.processFindUser(operation: AppOperation) = worker {
    this.name = "${Context::class.java.simpleName} :: process get-user"
    process {
        val uid = this.normalizedRequestAppAuthId
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
                fieldName = this.normalizedRequestAppAuthId.toFieldName(),
                description = "exception while get user",
                exception = it,
            )
        )
    }
}