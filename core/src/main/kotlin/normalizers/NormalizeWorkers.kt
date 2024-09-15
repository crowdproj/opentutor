package com.gitlab.sszuev.flashcards.core.normalizers

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker
import com.gitlab.sszuev.flashcards.model.Id
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardFilter
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardOperation
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryOperation
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet

fun ChainDSL<DictionaryContext>.normalizers(operation: DictionaryOperation) = worker(
    name = "Make a normalized copy of ${operation.name.lowercase()} params"
) {
    this.normalizedRequestAppAuthId = this.requestAppAuthId.normalize()
    when (operation) {
        DictionaryOperation.DELETE_DICTIONARY -> {
            this.normalizedRequestDictionaryId = this.requestDictionaryId.normalize()
        }

        DictionaryOperation.DOWNLOAD_DICTIONARY -> {
            this.normalizedRequestDictionaryId = this.requestDictionaryId.normalize()
            this.normalizedRequestDownloadDocumentType = this.requestDownloadDocumentType.trim().lowercase()
        }

        DictionaryOperation.UPLOAD_DICTIONARY -> {
            this.normalizedRequestDownloadDocumentType = this.requestDownloadDocumentType.trim().lowercase()
        }

        DictionaryOperation.CREATE_DICTIONARY, DictionaryOperation.UPDATE_DICTIONARY -> {
            this.normalizedRequestDictionaryEntity = this.requestDictionaryEntity.normalize()
        }

        else -> {}
    }
}

fun ChainDSL<TTSContext>.normalizers(operation: TTSOperation) = worker(
    name = "Make a normalized copy of ${operation.name.lowercase()} params"
) {
    this.normalizedRequestAppAuthId = this.requestAppAuthId.normalize()
    when (operation) {
        TTSOperation.GET_RESOURCE -> {
            this.normalizedRequestTTSResourceGet = this.requestTTSResourceGet.normalize()
        }

        else -> {}
    }
}

fun ChainDSL<SettingsContext>.normalizers(operation: SettingsOperation) = worker(
    name = "Make a normalized copy of ${operation.name.lowercase()} params"
) {
    this.normalizedRequestAppAuthId = this.requestAppAuthId.normalize()
}

fun ChainDSL<CardContext>.normalizers(operation: CardOperation) = worker(
    name = "Make a normalized copy of ${operation.name.lowercase()} params"
) {
    this.normalizedRequestAppAuthId = this.requestAppAuthId.normalize()
    when (operation) {
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

fun CardEntity.normalize() = CardEntity(
    cardId = this.cardId.normalize(),
    dictionaryId = this.dictionaryId.normalize(),
    words = this.words.map { it.normalize() },
    details = this.details,
    stats = this.stats,
    answered = this.answered,
    changedAt = this.changedAt,
)

fun CardWordEntity.normalize() = CardWordEntity(
    word = this.word.trim(),
    transcription = this.transcription?.trim(),
    partOfSpeech = this.partOfSpeech?.lowercase()?.trim(),
    translations = this.translations.map { it.map { t -> t.trim() } },
    examples = this.examples.map { it.normalize() },
)

fun CardWordExampleEntity.normalize() = CardWordExampleEntity(
    text = this.text.trim(),
    translation = this.translation?.trim(),
)

fun DictionaryEntity.normalize() = DictionaryEntity(
    dictionaryId = this.dictionaryId.normalize(),
    name = this.name.trim(),
    sourceLang = this.sourceLang.normalize(),
    targetLang = this.targetLang.normalize(),
    totalCardsCount = this.totalCardsCount,
    learnedCardsCount = this.learnedCardsCount,
    userId = this.userId.normalize(),
    numberOfRightAnswers = this.numberOfRightAnswers,
)

fun LangEntity.normalize() = LangEntity(
    langId = this.langId.normalize(),
    partsOfSpeech = this.partsOfSpeech.map { it.trim() }
)

fun CardFilter.normalize(): CardFilter {
    return CardFilter(
        dictionaryIds = this.dictionaryIds.map { it.normalize() },
        random = this.random,
        length = this.length,
        onlyUnknown = this.onlyUnknown,
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