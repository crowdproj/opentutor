package com.gitlab.sszuev.flashcards.mappers.v1.testutils

import com.gitlab.sszuev.flashcards.api.v1.models.CardResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordExampleResource
import com.gitlab.sszuev.flashcards.api.v1.models.CardWordResource
import com.gitlab.sszuev.flashcards.api.v1.models.DictionaryResource
import com.gitlab.sszuev.flashcards.api.v1.models.ErrorResource
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.NONE
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.junit.jupiter.api.Assertions
import java.time.ZoneOffset


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
    Assertions.assertEquals(expected.words?.size, actual.words.size)
    Assertions.assertEquals(expected.changedAt?.toInstant()?.toKotlinInstant() ?: Instant.NONE, actual.changedAt)
    Assertions.assertEquals(expected.details ?: emptyMap<String, Any>(), actual.details)
    Assertions.assertEquals(
        expected.stats?.mapKeys { it.key.uppercase().replace("-", "_") } ?: emptyMap<String, Long>(),
        actual.stats.mapKeys { it.key.name }
    )
    Assertions.assertEquals(expected.answered, actual.answered)
    expected.words!!.forEachIndexed { index, expectedResource ->
        assertWord(expectedResource, actual.words[index])
    }
}

internal fun assertCard(expected: CardEntity, actual: CardResource) {
    Assertions.assertEquals(if (expected.cardId != CardId.NONE) expected.cardId.asString() else null, actual.cardId)
    Assertions.assertEquals(expected.dictionaryId.asString(), actual.dictionaryId)
    Assertions.assertEquals(expected.words.size, actual.words?.size)
    Assertions.assertEquals(
        expected.changedAt.toJavaInstant().atOffset(ZoneOffset.UTC),
        actual.changedAt ?: Instant.NONE.toJavaInstant().atOffset(ZoneOffset.UTC)
    )
    Assertions.assertEquals(expected.details, actual.details ?: emptyMap<String, Any>())
    Assertions.assertEquals(
        expected.stats.mapKeys { it.key.name.lowercase().replace("_", "-") },
        actual.stats
    )
    Assertions.assertEquals(expected.answered, actual.answered)
    expected.words.forEachIndexed { index, entity ->
        assertWord(entity, actual.words!![index])
    }
}

private fun assertWord(expected: CardWordResource, actual: CardWordEntity) {
    Assertions.assertEquals(expected.word, actual.word)
    Assertions.assertEquals(expected.partOfSpeech, actual.partOfSpeech)
    Assertions.assertEquals(expected.sound ?: "", actual.sound.asString())
    Assertions.assertEquals(expected.transcription, actual.transcription)
    Assertions.assertEquals(expected.translations ?: emptyList<List<String>>(), actual.translations)
    Assertions.assertEquals(expected.examples?.size, actual.examples.size)
    expected.examples!!.forEachIndexed { index, resource ->
        Assertions.assertTrue(examplesEquals(resource, actual.examples[index])) {
            "expected: $resource, actual: ${actual.examples[index]}"
        }
    }
}

private fun assertWord(expected: CardWordEntity, actual: CardWordResource) {
    Assertions.assertEquals(expected.word, actual.word)
    Assertions.assertEquals(expected.partOfSpeech, actual.partOfSpeech)
    Assertions.assertEquals(expected.sound.asString(), actual.sound ?: "")
    Assertions.assertEquals(expected.transcription, actual.transcription)
    Assertions.assertEquals(expected.translations, actual.translations ?: emptyList<List<String>>())
    Assertions.assertEquals(expected.examples.size, actual.examples?.size)
    actual.examples!!.forEachIndexed { index, resource ->
        Assertions.assertTrue(examplesEquals(resource, expected.examples[index])) {
            "expected: ${expected.examples[index]}, actual: $resource"
        }
    }
}

private fun examplesEquals(left: CardWordExampleResource, right: CardWordExampleEntity): Boolean =
    left.translation == right.translation && (left.example ?: "") == right.text

internal fun assertCardId(expected: String?, actual: CardId) {
    Assertions.assertEquals(expected?.let { CardId(it) } ?: CardId.NONE, actual)
}

internal fun assertError(expected: AppError, actual: ErrorResource) {
    Assertions.assertEquals(expected.code, actual.code)
    Assertions.assertEquals(expected.message, actual.message)
    Assertions.assertEquals(expected.field, actual.field)
    Assertions.assertEquals(expected.group, actual.group)
}