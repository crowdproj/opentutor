package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.dbmem.dao.Example
import com.gitlab.sszuev.flashcards.dbmem.dao.Translation
import com.gitlab.sszuev.flashcards.model.domain.Stage
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
            translations = listOf(
                Translation(id = 1, cardId = 1, text = "снегопад"),
            ),
            examples = listOf(
                Example(id = 1, cardId = 1, text = "Due to the heavy snowfall, all flights have been cancelled..."),
            ),
            answered = 42,
            details = """
                { "MOSAIC": 42, "WRITING": 21 }
            """.trimIndent()
        )
        val expectedTranslations = listOf(card.translations.map { it.text })
        val expectedDetails = mapOf(Stage.MOSAIC to 42L, Stage.WRITING to 21L)
        val res = card.toEntity()
        Assertions.assertEquals(card.text, res.word)
        Assertions.assertEquals(card.id.toString(), res.cardId.asString())
        Assertions.assertEquals(card.dictionaryId.toString(), res.dictionaryId.asString())
        Assertions.assertEquals(card.partOfSpeech, res.partOfSpeech)
        Assertions.assertEquals(card.transcription, res.transcription)
        Assertions.assertEquals(expectedDetails, res.details)
        Assertions.assertEquals(card.answered, res.answered)
        Assertions.assertEquals(expectedTranslations, res.translations)
        Assertions.assertEquals(card.examples.map { it.text }.toList(), res.examples)
    }
}