package com.gitlab.sszuev.flashcards.stubs

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppStub
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardLearn
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

const val STUB_ERROR_GROUP = "StubErrors"

val stubDictionary = DictionaryEntity(
    dictionaryId = DictionaryId(42.toString()),
    name = "Stub-dictionary",
    sourceLang = LangEntity(LangId("SL"), listOf("A", "B", "C")),
    targetLang = LangEntity(LangId("TL"), listOf("X", "Y")),
)

val stubDictionaries = listOf(stubDictionary)

val stubAudioResource = ResourceEntity(
    resourceId = TTSResourceId(42.toString()),
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
    cardId = CardId(42.toString()),
    dictionaryId = DictionaryId(42.toString()),
    words = listOf(
        CardWordEntity(
            word = "stub",
            partOfSpeech = "noun",
            transcription = "stʌb",
            translations = listOf(listOf("заглушка"), listOf("корешок", "талон", "квитанция")),
            examples = listOf("That was the last candle stub I had.", "\$500 ticket stub.").map {
                CardWordExampleEntity(
                    it
                )
            },
            sound = TTSResourceId("en:stub"),
        ),
    ),
    stats = mapOf(Stage.SELF_TEST to 42, Stage.OPTIONS to 21),
    sound = TTSResourceId("en:stub"),
)

val stubCards = IntRange(1, 3)
    .flatMap { dictionaryId -> IntRange(1, 42).map { cardId -> dictionaryId to cardId } }
    .map {
        stubCard.copy(
            cardId = CardId(it.second.toString()),
            dictionaryId = DictionaryId(it.first.toString()),
            words = listOf(
                CardWordEntity(
                    word = "XXX-${it.first}-${it.second}"
                ),
            ),
            sound = TTSResourceId.NONE,
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