package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.dbmem.dao.Example
import com.gitlab.sszuev.flashcards.dbmem.dao.Translation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class EntityMapperTest {

    @Test
    fun `test map card`() {
        val card = Card(
            id = 1,
            dictionaryId = 42,
            text = "snowfall",
            transcription = "ˈsnəʊfɔːl",
            partOfSpeech = "noun",
            translations = setOf(
                Translation(id = 1, cardId = 1, text = "снегопад"),
            ),
            examples = setOf(
                Example(id = 1, cardId = 1, text = "Due to the heavy snowfall, all flights have been cancelled..."),
            ),
            answered = 42,
        )
        val res = card.toEntity()
        Assertions.assertEquals(card.text, res.word)
        Assertions.assertEquals(card.id.toString(), res.cardId.asString())
        Assertions.assertEquals(card.dictionaryId.toString(), res.dictionaryId.asString())
        Assertions.assertEquals(card.partOfSpeech, res.partOfSpeech)
        Assertions.assertEquals(card.transcription, res.transcription)
        Assertions.assertEquals(card.details, res.details)
        Assertions.assertEquals(card.answered, res.answered)
        Assertions.assertEquals(card.translations.map { it.text }.toList(), res.translations)
        Assertions.assertEquals(card.examples.map { it.text }.toList(), res.examples)
    }
}