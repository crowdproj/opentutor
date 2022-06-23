package com.gitlab.sszuev.flashcards.core.stubs

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.stubs.stubError

fun ChainDSL<CardContext>.createCardSuccessStub() = worker {
    this.name = "Stub :: create-card success"
    test {
        this.debugCase == AppStub.SUCCESS && this.status == AppStatus.RUN
    }
    process {
        this.status = AppStatus.OK
        this.responseCardEntity = requestCardEntity
    }
}

fun ChainDSL<CardContext>.createCardFailUnknownStub() = worker {
    this.name = "Stub :: create-card fail unknown"
    test {
        this.debugCase == AppStub.UNKNOWN_ERROR && this.status == AppStatus.RUN
    }
    process {
        this.status = AppStatus.FAIL
        this.errors.add(
            stubError
        )
    }
}