package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceGet
import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.DeleteCardDbResponse

fun ChainDSL<CardContext>.processGetCard() = worker {
    this.name = "process get-card request"
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
    this.name = "process get-all-cards request"
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
    this.name = "process card-search request"
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
    this.name = "process create-card request"
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
    this.name = "process update-card request"
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
    this.name = "process learn-cards request"
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
    this.name = "process reset-cards request"
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
    this.name = "process delete-card request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val res = this.repositories.cardRepository(this.workMode).deleteCard(this.normalizedRequestCardEntityId)
        this.postProcess(res)
    }
    onException {
        this.handleThrowable(CardOperation.DELETE_CARD, it)
    }
}

private suspend fun CardContext.postProcess(res: CardsDbResponse) {
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
    }
    if (res.sourceLanguage != LangId.NONE) {
        val tts = this.repositories.ttsClientRepository(this.workMode)
        val responses = res.cards.associateWith { tts.findResourceId(ResourceGet(it.word, res.sourceLanguage).normalize()) }
        this.errors.addAll(responses.flatMap { it.value.errors })
        this.responseCardEntityList =
            responses.map { if (it.value.id != ResourceId.NONE) it.key.copy(sound = it.value.id) else it.key }
    } else {
        this.responseCardEntityList = res.cards
    }
    this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
}

private fun CardContext.postProcess(res: CardDbResponse) {
    this.responseCardEntity = res.card
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
    }
    this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
}

private fun CardContext.postProcess(res: DeleteCardDbResponse) {
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
    }
    this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
}