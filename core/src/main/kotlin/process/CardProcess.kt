package com.gitlab.sszuev.flashcards.core.process

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation

fun ChainDSL<CardContext>.processGetAllCardsRequest() = worker {
    this.name = "process get-all-cards-request"
    process {
        val id = this.normalizedRequestDictionaryId
        val res = this.repositories.cardRepository.getAllCards(id)
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

fun ChainDSL<CardContext>.processCreateRequest() = worker {
    this.name = "process create-card-request"
    process {
        val res = this.repositories.cardRepository.createCard(this.normalizedRequestCardEntity)
        if (res.errors.isNotEmpty()) {
            this.errors.addAll(res.errors)
            this.status = AppStatus.FAIL
        } else {
            this.responseCardEntity = res.card
            this.status = AppStatus.OK
        }
    }
    onException {
        fail(
            runError(
                operation = CardOperation.CREATE_CARD,
                description = "exception",
                exception = it
            )
        )
    }
}

fun ChainDSL<CardContext>.processCardSearchRequest() = worker {
    this.name = "process card-search-request"
    process {
        val res = this.repositories.cardRepository.searchCard(this.normalizedRequestCardFilter)
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
                operation = CardOperation.SEARCH_CARDS,
                description = "exception",
                exception = it
            )
        )
    }
}