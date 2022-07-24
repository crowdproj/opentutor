package com.gitlab.sszuev.flashcards.core.process

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.repositories.toDbRequest

fun ChainDSL<CardContext>.processGetAllCardsRequest() = worker {
    this.name = "process get-all-cards-request"
    process {
        val id = this.normalizedRequestDictionaryId
        val res = this.repositories.cardRepository.getAllCards(id.toDbRequest())
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
            this.status = AppStatus.FAIL
        } else {
            this.responseCardEntityList = res.cards
            this.status = AppStatus.OK
        }
    }
    onException {
        fail(
            runError(
                operation = CardOperation.GET_ALL_CARDS,
                fieldName = this.requestDictionaryId.toFieldName(),
                description = "exception",
                exception = it
            )
        )
    }
}
