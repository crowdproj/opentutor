package com.gitlab.sszuev.flashcards.core.normalizers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.*

fun ChainDSL<DictionaryContext>.normalizers(operation: DictionaryOperation) = worker(
    name = "Make a normalized copy of ${operation.name.lowercase()} params"
) {
    this.normalizedRequestAppAuthId = this.requestAppAuthId.normalize()
    when (operation) {
        DictionaryOperation.DELETE_DICTIONARY, DictionaryOperation.DOWNLOAD_DICTIONARY -> {
            this.normalizedRequestDictionaryId = this.requestDictionaryId.normalize()
        }
        else -> {}
    }
}

fun ChainDSL<CardContext>.normalizers(operation: CardOperation) = worker(
    name = "Make a normalized copy of ${operation.name.lowercase()} params"
) {
    this.normalizedRequestAppAuthId = this.requestAppAuthId.normalize()
    when (operation) {
        CardOperation.GET_RESOURCE -> {
            this.normalizedRequestTTSResourceGet = this.requestTTSResourceGet.normalize()
        }

        CardOperation.SEARCH_CARDS -> {
            this.normalizedRequestCardFilter = this.requestCardFilter.normalize()
        }

        CardOperation.GET_ALL_CARDS -> {
            this.normalizedRequestDictionaryId = this.requestDictionaryId.normalize()
        }

        CardOperation.GET_CARD, CardOperation.RESET_CARD, CardOperation.DELETE_CARD -> {
            this.normalizedRequestCardEntityId = this.requestCardEntityId.normalize()
        }

        CardOperation.CREATE_CARD, CardOperation.UPDATE_CARD -> {
            this.normalizedRequestCardEntity = this.requestCardEntity.normalize()
        }

        CardOperation.LEARN_CARDS -> {
            this.normalizedRequestCardLearnList = this.requestCardLearnList.map { it.normalize() }
        }

        else -> {}
    }
}

fun CardEntity.normalize(): CardEntity {
    return CardEntity(
        cardId = this.cardId.normalize(),
        dictionaryId = this.dictionaryId.normalize(),
        word = this.word.trim(),
        transcription = this.transcription?.trim(),
        partOfSpeech = this.partOfSpeech?.lowercase()?.trim(),
        details = this.details,
        answered = this.answered,
        translations = this.translations,
        examples = this.examples.asSequence().map { it.trim() }.filter { it.isNotBlank() }.toList(),
    )
}

fun CardFilter.normalize(): CardFilter {
    return CardFilter(
        dictionaryIds = this.dictionaryIds.map { it.normalize() },
        random = this.random,
        length = this.length,
        withUnknown = this.withUnknown,
    )
}

fun CardLearn.normalize(): CardLearn {
    return CardLearn(
        cardId = this.cardId.normalize(),
        details = this.details,
    )
}

fun TTSResourceGet.normalize(): TTSResourceGet {
    return TTSResourceGet(
        word = this.word.trim(),
        lang = this.lang.normalize(),
    )
}

fun AppAuthId.normalize(): AppAuthId {
    return AppAuthId(this.normalizeAsString())
}

fun CardId.normalize(): CardId {
    return CardId(this.normalizeAsString())
}

fun DictionaryId.normalize(): DictionaryId {
    return DictionaryId(this.normalizeAsString())
}

fun LangId.normalize(): LangId {
    return LangId(this.normalizeAsString().lowercase())
}

private fun Id.normalizeAsString(): String {
    return asString().trim()
}