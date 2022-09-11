package com.gitlab.sszuev.flashcards.core.stubs

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.core.validators.fail
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppContext
import com.gitlab.sszuev.flashcards.model.common.AppOperation
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.stubErrorForCode

fun ChainDSL<DictionaryContext>.dictionaryStubSuccess(
    operation: DictionaryOperation,
    configure: DictionaryContext.() -> Unit = {}
) = stubSuccess(operation, configure)

fun ChainDSL<CardContext>.cardStubSuccess(
    operation: CardOperation,
    configure: CardContext.() -> Unit = {}
) = stubSuccess(operation, configure)

fun <Context : AppContext> ChainDSL<Context>.stubSuccess(
    operation: AppOperation,
    configure: Context.() -> Unit = {}
) = worker {
    name = "Stub :: ${operation.name} success"
    test {
        this.debugCase == AppStub.SUCCESS && this.status == AppStatus.RUN
    }
    process {
        this.status = AppStatus.OK
        this.configure()
    }
}

fun <Context : AppContext> ChainDSL<Context>.stubError(operation: AppOperation) = worker {
    this.name = "stub :: ${operation.name} fail unknown"
    test {
        this.debugCase == AppStub.UNKNOWN_ERROR && this.status == AppStatus.RUN
    }
    process {
        fail(stubError)
    }
}

fun <Context : AppContext> ChainDSL<Context>.stubError(operation: AppOperation, debugCase: AppStub) =
    stubError("Stub :: ${operation.name} fail ${debugCase.name}", debugCase)

private fun <Context : AppContext> ChainDSL<Context>.stubError(name: String, debugCase: AppStub) = worker {
    this.name = name
    test {
        this.debugCase == debugCase && this.status == AppStatus.RUN
    }
    process {
        fail(stubErrorForCode(debugCase))
    }
}