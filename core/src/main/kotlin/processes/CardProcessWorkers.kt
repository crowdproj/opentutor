package com.gitlab.sszuev.flashcards.core.processes

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.core.mappers.toCardEntity
import com.gitlab.sszuev.flashcards.core.mappers.toDbCard
import com.gitlab.sszuev.flashcards.core.mappers.toDictionaryEntity
import com.gitlab.sszuev.flashcards.core.normalizers.normalize
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.common.AppStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.gitlab.sszuev.flashcards.core.processes.CardProcessWorkers")

fun ChainDSL<CardContext>.processGetCard() = worker {
    this.name = "process get-card request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId
        val cardId = this.normalizedRequestCardEntityId
        val card = this.repositories.cardRepository.findCardById(cardId.asString())?.toCardEntity()?.normalize()
        if (card == null) {
            this.errors.add(noCardFoundDataError(CardOperation.GET_CARD, cardId))
        } else {
            val dictionary = this.repositories.dictionaryRepository
                .findDictionaryById(card.dictionaryId.asString())?.toDictionaryEntity(config)?.normalize()
            if (dictionary == null) {
                this.errors.add(
                    noDictionaryFoundDataError(
                        CardOperation.GET_CARD,
                        card.dictionaryId,
                        normalizedRequestAppAuthId
                    )
                )
            } else if (dictionary.userId != userId) {
                this.errors.add(forbiddenEntityDataError(CardOperation.GET_CARD, card.cardId, userId))
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
        val userId = this.normalizedRequestAppAuthId
        val dictionaryId = this.normalizedRequestDictionaryId
        val dictionary =
            this.repositories.dictionaryRepository.findDictionaryById(dictionaryId.asString())
                ?.toDictionaryEntity(config)?.normalize()
        if (dictionary == null) {
            this.errors.add(
                noDictionaryFoundDataError(
                    CardOperation.GET_ALL_CARDS,
                    dictionaryId,
                    normalizedRequestAppAuthId
                )
            )
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError(CardOperation.GET_ALL_CARDS, dictionaryId, userId))
        } else {
            val cards = postProcess(
                this.repositories.cardRepository
                    .findCardsByDictionaryId(dictionaryId.asString()).map { it.toCardEntity().normalize() }.iterator()
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
        val userId = this.normalizedRequestAppAuthId
        val foundDictionaries = this.repositories.dictionaryRepository
            .findDictionariesByIdIn(this.normalizedRequestCardFilter.dictionaryIds.map { it.asString() })
            .map { it.toDictionaryEntity(config).normalize() }
            .associateBy { it.dictionaryId }
        this.normalizedRequestCardFilter.dictionaryIds.filterNot { foundDictionaries.containsKey(it) }.forEach {
            this.errors.add(noDictionaryFoundDataError(CardOperation.SEARCH_CARDS, it, normalizedRequestAppAuthId))
        }
        foundDictionaries.values.forEach { dictionary ->
            if (dictionary.userId != userId) {
                this.errors.add(forbiddenEntityDataError(CardOperation.SEARCH_CARDS, dictionary.dictionaryId, userId))
            }
        }
        if (errors.isEmpty()) {
            val cards = postProcess(findCardDeck(foundDictionaries).iterator()) { checkNotNull(foundDictionaries[it]) }
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
        val userId = this.normalizedRequestAppAuthId
        val dictionaryId = this.normalizedRequestCardEntity.dictionaryId
        val dictionary = this.repositories.dictionaryRepository
            .findDictionaryById(dictionaryId.asString())?.toDictionaryEntity(config)?.normalize()
        if (dictionary == null) {
            this.errors.add(
                noDictionaryFoundDataError(
                    operation = CardOperation.CREATE_CARD,
                    id = dictionaryId,
                    userId = normalizedRequestAppAuthId
                )
            )
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError(CardOperation.CREATE_CARD, dictionaryId, userId))
        } else {
            val res = this.repositories.cardRepository
                .createCard(this.normalizedRequestCardEntity.toDbCard()).toCardEntity().normalize()
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
        val userId = this.normalizedRequestAppAuthId
        val dictionaryId = this.normalizedRequestCardEntity.dictionaryId
        val dictionary = this.repositories.dictionaryRepository
            .findDictionaryById(dictionaryId.asString())?.toDictionaryEntity(config)?.normalize()
        if (dictionary == null) {
            this.errors.add(
                noDictionaryFoundDataError(
                    operation = CardOperation.UPDATE_CARD,
                    id = dictionaryId,
                    userId = normalizedRequestAppAuthId
                )
            )
        } else if (dictionary.userId != userId) {
            this.errors.add(forbiddenEntityDataError(CardOperation.UPDATE_CARD, dictionaryId, userId))
        } else {
            if (logger.isDebugEnabled) {
                logger.debug("Update card request: {}", this.normalizedRequestCardEntity)
            }
            val res = this.repositories.cardRepository
                .updateCard(this.normalizedRequestCardEntity.toDbCard()).toCardEntity().normalize()
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
        val userId = this.normalizedRequestAppAuthId
        val cardLearns = this.normalizedRequestCardLearnList.associateBy { it.cardId }
        val foundCards = this.repositories.cardRepository
            .findCardsByIdIn(cardLearns.keys.map { it.asString() }).map { it.toCardEntity().normalize() }.toSet()
        val foundCardIds = foundCards.map { it.cardId }.toSet()
        val missedCardIds = cardLearns.keys - foundCardIds
        missedCardIds.forEach {
            errors.add(noCardFoundDataError(CardOperation.LEARN_CARDS, it))
        }
        val dictionaryIds = foundCards.map { it.dictionaryId }.toSet()
        val foundDictionaries =
            this.repositories.dictionaryRepository
                .findDictionariesByIdIn(dictionaryIds.map { it.asString() })
                .map { it.toDictionaryEntity(config).normalize() }
                .associateBy { it.dictionaryId }
        val missedDictionaries = dictionaryIds - foundDictionaries.keys
        missedDictionaries.forEach {
            errors.add(noDictionaryFoundDataError(CardOperation.LEARN_CARDS, it, userId))
        }
        foundDictionaries.onEach {
            if (it.value.userId != userId) {
                errors.add(forbiddenEntityDataError(CardOperation.LEARN_CARDS, it.key, userId))
            }
        }
        if (errors.isEmpty()) {
            this.responseCardEntityList = postProcess(learnCards(foundCards, cardLearns).iterator()) {
                checkNotNull(foundDictionaries[it])
            }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(CardOperation.LEARN_CARDS, it)
    }
}

fun ChainDSL<CardContext>.processResetCard() = worker {
    this.name = "process reset-cards request"
    test {
        this.status == AppStatus.RUN
    }
    process {
        val userId = this.normalizedRequestAppAuthId
        val cardId = this.normalizedRequestCardEntityId
        val card = this.repositories.cardRepository.findCardById(cardId.asString())?.toCardEntity()?.normalize()
        if (card == null) {
            this.errors.add(noCardFoundDataError(CardOperation.RESET_CARD, cardId))
        } else {
            val dictionaryId = card.dictionaryId
            val dictionary = this.repositories.dictionaryRepository
                .findDictionaryById(dictionaryId.asString())?.toDictionaryEntity(config)?.normalize()
            if (dictionary == null) {
                this.errors.add(
                    noDictionaryFoundDataError(
                        operation = CardOperation.RESET_CARD,
                        id = dictionaryId,
                        userId = normalizedRequestAppAuthId
                    )
                )
            } else if (dictionary.userId != userId) {
                this.errors.add(forbiddenEntityDataError(CardOperation.RESET_CARD, dictionaryId, userId))
            } else {
                val res = this.repositories.cardRepository.updateCard(card.copy(answered = 0).toDbCard())
                    .toCardEntity().normalize()
                this.responseCardEntity = postProcess(res) { dictionary }
            }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
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
        val userId = this.normalizedRequestAppAuthId
        val cardId = this.normalizedRequestCardEntityId
        val card = this.repositories.cardRepository.findCardById(cardId.asString())?.toCardEntity()?.normalize()
        if (card == null) {
            this.errors.add(noCardFoundDataError(CardOperation.DELETE_CARD, cardId))
        } else {
            val dictionary = this.repositories.dictionaryRepository
                .findDictionaryById(card.dictionaryId.asString())?.toDictionaryEntity(config)?.normalize()
            if (dictionary == null) {
                this.errors.add(
                    noDictionaryFoundDataError(
                        operation = CardOperation.DELETE_CARD,
                        id = card.dictionaryId,
                        userId = normalizedRequestAppAuthId
                    )
                )
            } else if (dictionary.userId != userId) {
                this.errors.add(forbiddenEntityDataError(CardOperation.DELETE_CARD, card.cardId, userId))
            } else {
                this.repositories.cardRepository
                    .deleteCard(this.normalizedRequestCardEntityId.asString())
            }
        }
        this.status = if (this.errors.isNotEmpty()) AppStatus.FAIL else AppStatus.RUN
    }
    onException {
        this.handleThrowable(CardOperation.DELETE_CARD, it)
    }
}

private fun postProcess(
    cardsIterator: Iterator<CardEntity>,
    dictionary: (DictionaryId) -> DictionaryEntity
): List<CardEntity> {
    val res = mutableListOf<CardEntity>()
    while (cardsIterator.hasNext()) {
        res.add(postProcess(cardsIterator.next(), dictionary))
    }
    return res
}

private fun postProcess(
    card: CardEntity,
    dictionary: (DictionaryId) -> DictionaryEntity
): CardEntity {
    check(card != CardEntity.EMPTY) { "Null card" }
    val sourceLang = dictionary.invoke(card.dictionaryId).sourceLang.langId
    val words = card.words.map { it.copy(sound = TTSResourceGet(it.word, sourceLang).asResourceId()) }

    val cardAudioId = if (words.size == 1) {
        words.single().sound
    } else {
        val cardAudioString = card.words.joinToString(",") { it.word.split("|")[0].trim() }
        TTSResourceGet(cardAudioString, sourceLang).asResourceId()
    }
    return card.copy(words = words, sound = cardAudioId)
}
