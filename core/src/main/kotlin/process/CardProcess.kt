package com.gitlab.sszuev.flashcards.core.process

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse

fun ChainDSL<CardContext>.processGetCardRequest() = worker {
    this.name = "process get-card-request"
    process {
        val id = this.normalizedRequestCardEntityId
        val res = this.repositories.cardRepository.getCard(id)
        this.postProcess(res)
    }
    onException {
        fail(
            runError(
                operation = CardOperation.GET_CARD,
                fieldName = this.requestCardEntityId.toFieldName(),
                description = "exception",
                exception = it
            )
        )
    }
}

fun ChainDSL<CardContext>.processGetAllCardsRequest() = worker {
    this.name = "process get-all-cards-request"
    process {
        val id = this.normalizedRequestDictionaryId
        val res = this.repositories.cardRepository.getAllCards(id)
        this.postProcess(res)
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

fun ChainDSL<CardContext>.processCardSearchRequest() = worker {
    this.name = "process card-search-request"
    process {
        val res = this.repositories.cardRepository.searchCard(this.normalizedRequestCardFilter)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.SEARCH_CARDS, it)
    }
}

fun ChainDSL<CardContext>.processCreateCardRequest() = worker {
    this.name = "process create-card-request"
    process {
        val res = this.repositories.cardRepository.createCard(this.normalizedRequestCardEntity)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.CREATE_CARD, it)
    }
}

fun ChainDSL<CardContext>.processUpdateCardRequest() = worker {
    this.name = "process update-card-request"
    process {
        val res = this.repositories.cardRepository.updateCard(this.normalizedRequestCardEntity)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.UPDATE_CARD, it)
    }
}

fun ChainDSL<CardContext>.processLearnCardsRequest() = worker {
    this.name = "process learn-cards-request"
    process {
        val res = this.repositories.cardRepository.learnCards(this.normalizedRequestCardLearnList)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.LEARN_CARDS, it)
    }
}

private fun CardContext.postProcess(res: CardEntitiesDbResponse) {
    this.responseCardEntityList = res.cards
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
        this.status = AppStatus.FAIL
    } else {
        this.status = AppStatus.OK
    }
}

private fun CardContext.postProcess(res: CardEntityDbResponse) {
    this.responseCardEntity = res.card
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
        this.status = AppStatus.FAIL
    } else {
        this.status = AppStatus.OK
    }
}

private fun CardContext.handleThrowable(operation: CardOperation, ex: Throwable) {
    fail(
        runError(
            operation = operation,
            description = "exception",
            exception = ex,
        )
    )
}