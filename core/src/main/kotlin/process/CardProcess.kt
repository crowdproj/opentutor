package com.gitlab.sszuev.flashcards.core.process

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.repositories.CardEntitiesDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardEntityDbResponse

fun ChainDSL<CardContext>.processGetCard() = worker {
    this.name = "process get-card-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val id = this.normalizedRequestCardEntityId
        val res = this.repositories.cardRepository(this.workMode).getCard(id)
        this.postProcess(res)
    }
    onException {
        fail(
            runError(
                operation = CardOperation.GET_CARD,
                fieldName = this.normalizedRequestCardEntityId.toFieldName(),
                description = "exception",
                exception = it
            )
        )
    }
}

fun ChainDSL<CardContext>.processGetAllCards() = worker {
    this.name = "process get-all-cards-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val id = this.normalizedRequestDictionaryId
        val res = this.repositories.cardRepository(this.workMode).getAllCards(id)
        this.postProcess(res)
    }
    onException {
        fail(
            runError(
                operation = CardOperation.GET_ALL_CARDS,
                fieldName = this.normalizedRequestDictionaryId.toFieldName(),
                description = "exception",
                exception = it
            )
        )
    }
}

fun ChainDSL<CardContext>.processCardSearch() = worker {
    this.name = "process card-search-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.cardRepository(this.workMode).searchCard(this.normalizedRequestCardFilter)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.SEARCH_CARDS, it)
    }
}

fun ChainDSL<CardContext>.processCreateCard() = worker {
    this.name = "process create-card-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.cardRepository(this.workMode).createCard(this.normalizedRequestCardEntity)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.CREATE_CARD, it)
    }
}

fun ChainDSL<CardContext>.processUpdateCard() = worker {
    this.name = "process update-card-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.cardRepository(this.workMode).updateCard(this.normalizedRequestCardEntity)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.UPDATE_CARD, it)
    }
}

fun ChainDSL<CardContext>.processLearnCards() = worker {
    this.name = "process learn-cards-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.cardRepository(this.workMode).learnCards(this.normalizedRequestCardLearnList)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.LEARN_CARDS, it)
    }
}

fun ChainDSL<CardContext>.processResetCards() = worker {
    this.name = "process reset-cards-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.cardRepository(this.workMode).resetCard(this.normalizedRequestCardEntityId)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.RESET_CARD, it)
    }
}

fun ChainDSL<CardContext>.processDeleteCard() = worker {
    this.name = "process delete-card-request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.cardRepository(this.workMode).resetCard(this.normalizedRequestCardEntityId)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.DELETE_CARD, it)
    }
}

private fun CardContext.postProcess(res: CardEntitiesDbResponse) {
    this.responseCardEntityList = res.cards
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
    }
    this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
}

private fun CardContext.postProcess(res: CardEntityDbResponse) {
    this.responseCardEntity = res.card
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
    }
    this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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