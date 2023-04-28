package com.gitlab.sszuev.flashcards.mappers.v1.testutils

import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.ErrorResource
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import org.junit.jupiter.api.Assertions


internal fun assertDictionary(expected: DictionaryEntity, actual: DictionaryResource) {
    Assertions.assertEquals(
        if (expected.dictionaryId == DictionaryId.NONE) null else expected.dictionaryId.asString(),
        actual.dictionaryId
    )
    Assertions.assertEquals(expected.name, actual.name)
    Assertions.assertEquals(
        if (expected.sourceLang == LangEntity.EMPTY) null else expected.sourceLang.langId.asString(),
        actual.sourceLang
    )
    Assertions.assertEquals(
        if (expected.targetLang == LangEntity.EMPTY) null else expected.targetLang.langId.asString(),
        actual.targetLang
    )
    Assertions.assertEquals(
        expected.sourceLang.takeIf { it != LangEntity.EMPTY }?.partsOfSpeech,
        actual.partsOfSpeech
    )
    Assertions.assertEquals(expected.totalCardsCount, actual.total)
    Assertions.assertEquals(expected.learnedCardsCount, actual.learned)
}

internal fun assertDictionary(expected: DictionaryResource, actual: DictionaryEntity) {
    Assertions.assertEquals(
        if (expected.dictionaryId == null) DictionaryId.NONE else DictionaryId(expected.dictionaryId!!),
        actual.dictionaryId
    )
    Assertions.assertEquals(expected.name, actual.name)
    Assertions.assertEquals(
        if (expected.sourceLang == null) LangId.NONE else LangId(expected.sourceLang!!),
        actual.sourceLang.langId
    )
    Assertions.assertEquals(
        if (expected.targetLang == null) LangId.NONE else LangId(expected.targetLang!!),
        actual.targetLang.langId
    )
    Assertions.assertEquals(
        if (expected.partsOfSpeech == null) emptyList() else expected.partsOfSpeech!!,
        actual.sourceLang.partsOfSpeech
    )
    Assertions.assertEquals(expected.total, actual.totalCardsCount)
    Assertions.assertEquals(expected.learned, actual.learnedCardsCount)
}

internal fun assertCard(expected: CardResource, actual: CardEntity) {
    assertCardId(expected.cardId, actual.cardId)
    Assertions.assertEquals(expected.dictionaryId, actual.dictionaryId.asString())
    Assertions.assertEquals(expected.word, actual.words.single().word)
}

internal fun assertCard(expected: CardEntity, actual: CardResource) {
    Assertions.assertEquals(if (expected.cardId != CardId.NONE) expected.cardId.asString() else null, actual.cardId)
    Assertions.assertEquals(expected.dictionaryId.asString(), actual.dictionaryId)
    Assertions.assertEquals(expected.words.single().word, actual.word)
}

internal fun assertCardId(expected: String?, actual: CardId) {
    Assertions.assertEquals(expected?.let { CardId(it) } ?: CardId.NONE, actual)
}

internal fun assertError(expected: AppError, actual: ErrorResource) {
    Assertions.assertEquals(expected.code, actual.code)
    Assertions.assertEquals(expected.message, actual.message)
    Assertions.assertEquals(expected.field, actual.field)
    Assertions.assertEquals(expected.group, actual.group)
}