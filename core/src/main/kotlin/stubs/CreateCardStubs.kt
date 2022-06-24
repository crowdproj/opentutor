package com.gitlab.sszuev.flashcards.core.stubs

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.validation.fail
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.stubs.stubCard
import com.gitlab.sszuev.flashcards.stubs.stubCards
import com.gitlab.sszuev.flashcards.stubs.stubError
import com.gitlab.sszuev.flashcards.stubs.stubErrorForCode

fun ChainDSL<CardContext>.createCardSuccessStub() = worker {
    this.name = "Stub :: create-card success"
    test {
        this.debugCase == AppStub.SUCCESS && this.status == AppStatus.RUN
    }
    process {
        this.status = AppStatus.OK
        this.responseCardEntity = stubCard
    }
}

fun ChainDSL<CardContext>.searchCardsSuccessStub() = worker {
    this.name = "Stub :: search-cards success"
    test {
        this.debugCase == AppStub.SUCCESS && this.status == AppStatus.RUN
    }
    process {
        this.status = AppStatus.OK
        this.responseCardEntityList = stubCards
    }
}

fun ChainDSL<CardContext>.unknownErrorStub(name: String) = worker {
    this.name = name
    test {
        this.debugCase == AppStub.UNKNOWN_ERROR && this.status == AppStatus.RUN
    }
    process {
        fail(stubError)
    }
}

fun ChainDSL<CardContext>.searchCardsErrorStub(debugCase: AppStub) = worker {
    errorStub("Stub :: search-cards fail ${debugCase.name}", debugCase)
}

fun ChainDSL<CardContext>.createCardErrorStub(debugCase: AppStub) = worker {
    errorStub("Stub :: create-card fail ${debugCase.name}", debugCase)
}

fun ChainDSL<CardContext>.errorStub(name: String, debugCase: AppStub) = worker {
    this.name = name
    test {
        this.debugCase == debugCase && this.status == AppStatus.RUN
    }
    process {
        fail(stubErrorForCode(debugCase))
    }
}