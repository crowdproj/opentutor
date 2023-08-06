package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.CardsDbResponse
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse

fun ChainDSL<CardContext>.processGetCard() = worker {
    this.name = "process get-card request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.contextUserEntity.id
        val cardId = this.normalizedRequestCardEntityId
        val res = this.repositories.cardRepository(this.workMode).getCard(userId, cardId)
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
        val userId = this.contextUserEntity.id
        val dictionaryId = this.normalizedRequestDictionaryId
        val res = this.repositories.cardRepository(this.workMode).getAllCards(userId, dictionaryId)
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
        val userId = this.contextUserEntity.id
        val res = this.repositories.cardRepository(this.workMode).searchCard(userId, this.normalizedRequestCardFilter)
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
        val userId = this.contextUserEntity.id
        val res = this.repositories.cardRepository(this.workMode).createCard(userId, this.normalizedRequestCardEntity)
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
        val userId = this.contextUserEntity.id
        val res = this.repositories.cardRepository(this.workMode).updateCard(userId, this.normalizedRequestCardEntity)
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
        val userId = this.contextUserEntity.id
        val res =
            this.repositories.cardRepository(this.workMode).learnCards(userId, this.normalizedRequestCardLearnList)
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
        val userId = this.contextUserEntity.id
        val res = this.repositories.cardRepository(this.workMode).resetCard(userId, this.normalizedRequestCardEntityId)
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
        val userId = this.contextUserEntity.id
        val res = this.repositories.cardRepository(this.workMode).removeCard(userId, this.normalizedRequestCardEntityId)
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
    if (res.sourceLanguageId != LangId.NONE) {
        val tts = this.repositories.ttsClientRepository(this.workMode)
        this.responseCardEntityList = res.cards.map { card ->
            val words = card.words.map { word ->
                val r = tts.findResourceId(TTSResourceGet(word.word, res.sourceLanguageId).normalize())
                this.errors.addAll(r.errors)
                if (r.id != TTSResourceId.NONE) word.copy(sound = r.id) else word
            }
            card.copy(words = words)
        }
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

private fun CardContext.postProcess(res: RemoveCardDbResponse) {
    if (res.errors.isNotEmpty()) {
        this.errors.addAll(res.errors)
    }
    this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
}