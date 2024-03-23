package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.CardDbResponse
import com.gitlab.sszuev.flashcards.repositories.RemoveCardDbResponse

fun ChainDSL<CardContext>.processGetCard() = worker {
    this.name = "process get-card request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.contextUserEntity.id
        val cardId = this.normalizedRequestCardEntityId
        val card = this.repositories.cardRepository(this.workMode).findCardById(cardId)
        if (card == null) {
            this.errors.add(noCardFoundDataError("getCard", cardId))
        } else {
            val dictionary = this.repositories.dictionaryRepository(this.workMode).findDictionaryById(card.dictionaryId)
            if (dictionary == null) {
                this.errors.add(noDictionaryFoundDataError("getCard", card.dictionaryId))
            } else if (dictionary.userId != userId) {
                this.errors.add(forbiddenEntityDataError("getCard", card.cardId, userId))
            } else {
                this.responseCardEntity = postProcess(card) { dictionary }
            }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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
        val dictionary = this.repositories.dictionaryRepository(this.workMode).findDictionaryById(dictionaryId)
        if (dictionary == null) {
            this.errors.add(noDictionaryFoundDataError("getAllCards", dictionaryId))
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError("getAllCards", dictionaryId, userId))
        } else {
            val cards = postProcess(
                this.repositories.cardRepository(this.workMode).findCardsByDictionaryId(dictionaryId).iterator()
            ) { dictionary }
            this.responseCardEntityList = cards
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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
        val found = this.repositories.dictionaryRepository(this.workMode)
            .findDictionariesByIdIn(this.normalizedRequestCardFilter.dictionaryIds)
            .associateBy { it.dictionaryId }
        this.normalizedRequestCardFilter.dictionaryIds.filterNot { found.containsKey(it) }.forEach {
            this.errors.add(noDictionaryFoundDataError("searchCards", it))
        }
        found.values.forEach { dictionary ->
            if (dictionary.userId != userId) {
                this.errors.add(forbiddenEntityDataError("searchCards", dictionary.dictionaryId, userId))
            }
        }
        if (errors.isEmpty()) {
            val cards = postProcess(findCardDeck().iterator()) { checkNotNull(found[it]) }
            this.responseCardEntityList = cards
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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
        val dictionaryId = this.normalizedRequestCardEntity.dictionaryId
        val dictionary = this.repositories.dictionaryRepository(this.workMode).findDictionaryById(dictionaryId)
        if (dictionary == null) {
            this.errors.add(noDictionaryFoundDataError("createCard", dictionaryId))
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError("createCard", dictionaryId, userId))
        } else {
            val res = this.repositories.cardRepository(this.workMode).createCard(this.normalizedRequestCardEntity)
            this.responseCardEntity = postProcess(res) { dictionary }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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
        val dictionaryId = this.normalizedRequestCardEntity.dictionaryId
        val dictionary = this.repositories.dictionaryRepository(this.workMode).findDictionaryById(dictionaryId)
        if (dictionary == null) {
            this.errors.add(noDictionaryFoundDataError("updateCard", dictionaryId))
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError("updateCard", dictionaryId, userId))
        } else {
            val res = this.repositories.cardRepository(this.workMode).updateCard(this.normalizedRequestCardEntity)
            this.responseCardEntity = postProcess(res) { dictionary }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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
        val cardLearns = this.normalizedRequestCardLearnList.associateBy { it.cardId }
        val foundCards = this.repositories.cardRepository(this.workMode).findCardsByIdIn(cardLearns.keys).toSet()
        val foundCardIds = foundCards.map { it.cardId }.toSet()
        val missedCardIds = cardLearns.keys - foundCardIds
        missedCardIds.forEach {
            errors.add(noCardFoundDataError("learnCards", it))
        }
        val dictionaryIds = foundCards.map { it.dictionaryId }.toSet()
        val foundDictionaries =
            this.repositories.dictionaryRepository(this.workMode).findDictionariesByIdIn(dictionaryIds)
                .associateBy { it.dictionaryId }
        foundDictionaries.onEach {
            if (it.value.userId != userId) {
                errors.add(forbiddenEntityDataError("learnCards", it.key, userId))
            }
        }
        if (errors.isEmpty()) {
            this.responseCardEntityList =
                this.postProcess(learnCards(foundCards, cardLearns).iterator()) { checkNotNull(foundDictionaries[it]) }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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

private suspend fun CardContext.postProcess(
    cardsIterator: Iterator<CardEntity>,
    dictionary: (DictionaryId) -> DictionaryEntity
): List<CardEntity> {
    val res = mutableListOf<CardEntity>()
    while (cardsIterator.hasNext()) {
        res.add(postProcess(cardsIterator.next(), dictionary))
    }
    return res
}

private suspend fun CardContext.postProcess(
    card: CardEntity,
    dictionary: (DictionaryId) -> DictionaryEntity
): CardEntity {
    check(card != CardEntity.EMPTY) { "Null card" }
    val tts = this.repositories.ttsClientRepository(this.workMode)
    val sourceLang = dictionary.invoke(card.dictionaryId).sourceLang.langId
    val words = card.words.map { word ->
        val wordAudioId = tts.findResourceId(TTSResourceGet(word.word, sourceLang).normalize())
        this.errors.addAll(wordAudioId.errors)
        if (wordAudioId.id != TTSResourceId.NONE) {
            word.copy(sound = wordAudioId.id)
        } else {
            word
        }
    }

    val cardAudioId = if (words.size == 1) {
        words.single().sound
    } else {
        val cardAudioString = card.words.joinToString(",") { it.word }
        val findResourceIdResponse = tts.findResourceId(TTSResourceGet(cardAudioString, sourceLang).normalize())
        this.errors.addAll(findResourceIdResponse.errors)
        findResourceIdResponse.id
    }
    return card.copy(words = words, sound = cardAudioId)
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