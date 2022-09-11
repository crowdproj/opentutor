package com.gitlab.sszuev.flashcards.stubs

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.*

const val STUB_ERROR_GROUP = "StubErrors"

val stubDictionaries = listOf(
    DictionaryEntity(
        dictionaryId = DictionaryId(42.toString()),
        userId = AppUserId(1.toString()),
        name = "Stub-dictionary",
        sourceLangId = LangId("SL"),
        targetLangId = LangId("TL")
    )
)

val stubAudioResource = ResourceEntity(
    resourceId = ResourceId(42.toString()),
    data = ByteArray(42) { 42 },
)

val stubError = AppError(
    field = "the-error-field",
    exception = AssertionError("Test-error"),
    message = "the-error-message",
    code = "StubErrorCode",
    group = STUB_ERROR_GROUP,
)

val stubCard = CardEntity(
    word = "stub",
    cardId = CardId(42.toString()),
    dictionaryId = DictionaryId(42.toString()),
    partOfSpeech = "noun",
    transcription = "stʌb",
    translations = listOf(listOf("заглушка"), listOf("корешок", "талон", "квитация")),
    examples = listOf("That was the last candle stub I had.", "\$500 ticket stub."),
    details = mapOf(Stage.SELF_TEST to 42, Stage.OPTIONS to 21),
)

val stubCards = IntRange(1, 3)
    .flatMap { dictionaryId -> IntRange(1, 42).map { cardId -> dictionaryId to cardId } }
    .map {
        stubCard.copy(
            cardId = CardId(it.second.toString()),
            dictionaryId = DictionaryId(it.first.toString()),
            word = "XXX-${it.first}-${it.second}"
        )
    }

val stubLearnCardDetails = CardLearn(
    cardId = CardId(42.toString()),
    details = mapOf(Stage.SELF_TEST to 42, Stage.WRITING to 5, Stage.OPTIONS to 4)
)

fun stubErrorForCode(case: AppStub): AppError {
    return AppError(
        field = "field::$case",
        message = "the-error-message-for-$case",
        code = case.name,
        group = STUB_ERROR_GROUP,
    )
}