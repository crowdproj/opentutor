package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.LangEntity
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.Stage
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId

val testDictionaryEntity = DictionaryEntity(
    dictionaryId = DictionaryId(42.toString()),
    name = "Stub-dictionary",
    sourceLang = LangEntity(LangId("sl"), listOf("A", "B", "C")),
    targetLang = LangEntity(LangId("tl"), listOf("X", "Y")),
    userId = AppAuthId("00000000-0000-0000-0000-000000000000"),
    numberOfRightAnswers = 42,
)

val testDictionaryEntities = listOf(testDictionaryEntity)

val testCardEntity1 = CardEntity(
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
            sound = TTSResourceId("sl:stub"),
            primary = true,
        ),
    ),
    stats = mapOf(Stage.SELF_TEST to 42, Stage.OPTIONS to 21),
)

val testCardEntity2 = CardEntity(
    cardId = CardId(42.toString()),
    dictionaryId = DictionaryId(42.toString()),
    words = listOf(
        CardWordEntity(
            word = "q, w",
            translations = listOf(listOf("Q")),
            sound = TTSResourceId("sl:q,w"),
            primary = true,
        ),
        CardWordEntity(
            word = "q",
            translations = listOf(listOf("Q")),
            sound = TTSResourceId("sl:q"),
            primary = false,
        ),
        CardWordEntity(
            word = "w",
            sound = TTSResourceId("sl:w"),
            primary = false,
        ),
    ),
)

val testCardEntities = IntRange(1, 3)
    .flatMap { dictionaryId -> IntRange(1, 42).map { cardId -> dictionaryId to cardId } }
    .map {
        val word = "XXX-${it.first}-${it.second}"
        testCardEntity1.copy(
            cardId = CardId(it.second.toString()),
            dictionaryId = DictionaryId(it.first.toString()),
            words = listOf(
                CardWordEntity(
                    word = word,
                    sound = TTSResourceId("sl:$word"),
                    primary = true,
                ),
            ),
        )
    }
