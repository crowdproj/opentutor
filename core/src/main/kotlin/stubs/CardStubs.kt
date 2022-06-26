package com.gitlab.sszuev.flashcards.core.stubs

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.title
import com.gitlab.sszuev.flashcards.core.validation.fail
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.stubErrorForCode

fun ChainDSL<CardContext>.stubSuccess(
    operation: CardOperation,
    configure: CardContext.() -> Unit = {}
) = worker {
    name = "Stub :: ${operation.title()} success"
    test {
        this.debugCase == AppStub.SUCCESS && this.status == AppStatus.RUN
    }
    process {
        this.status = AppStatus.OK
        this.configure()
    }
}


fun ChainDSL<CardContext>.stubError(operation: CardOperation) = worker {
    this.name = "stub :: ${operation.title()} fail unknown"
    test {
        this.debugCase == AppStub.UNKNOWN_ERROR && this.status == AppStatus.RUN
    }
    process {
        fail(stubError)
    }
}

fun ChainDSL<CardContext>.stubError(operation: CardOperation, debugCase: AppStub) =
    stubError("Stub :: ${operation.title()} fail ${debugCase.name}", debugCase)

private fun ChainDSL<CardContext>.stubError(name: String, debugCase: AppStub) = worker {
    this.name = name
    test {
        this.debugCase == debugCase && this.status == AppStatus.RUN
    }
    process {
        fail(stubErrorForCode(debugCase))
    }
}