package com.gitlab.sszuev.flashcards.logs

import com.gitlab.sszuev.flashcards.logs.models.CardLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardWordExampleLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardWordLogResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

internal class LogResourcesSerializationTest {

    companion object {
        private val jacksonMapper = JsonMapper.builder().build()

        private fun serialize(response: Any): String =
            jacksonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response)
    }

    @Test
    fun `test CardLogResource serialization`() {
        val exampleA = CardWordExampleLogResource(text = "a", translation = "a-t")
        val exampleB = CardWordExampleLogResource(text = "b", translation = "b-t")
        val word1 = CardWordLogResource(
            examples = listOf(exampleA, exampleB),
            transcription = "xxx",
            translations = listOf(listOf("w"), listOf("q", "f")),
            partOfSpeech = "XXX",
            sound = "sss",
            word = "WWW",
        )
        val word2 = word1.copy(
            translations = null,
            examples = null,
        )
        val card = CardLogResource(
            cardId = "42",
            dictionaryId = "42",
            words = listOf(word1, word2),
            stats = mapOf("OPTIONS" to 42),
            answered = 42,
            changedAt = LocalDate.of(2013, Month.DECEMBER, 13).atTime(LocalTime.MIN)
                .atOffset(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.MINUTES),
            details = mapOf(
                "a" to 42L,
                "b" to listOf("a", 42),
                "c" to LocalDate.of(2006, Month.FEBRUARY, 13).atTime(LocalTime.MIN)
                    .atOffset(ZoneOffset.UTC)
                    .truncatedTo(ChronoUnit.MINUTES),
            )
        )
        val actual = serialize(card)
        val expected = """
            {
              "cardId" : "42",
              "dictionaryId" : "42",
              "words" : [ {
                "word" : "WWW",
                "transcription" : "xxx",
                "partOfSpeech" : "XXX",
                "translations" : [ [ "w" ], [ "q", "f" ] ],
                "examples" : [ {
                  "text" : "a",
                  "translation" : "a-t"
                }, {
                  "text" : "b",
                  "translation" : "b-t"
                } ],
                "sound" : "sss"
              }, {
                "word" : "WWW",
                "transcription" : "xxx",
                "partOfSpeech" : "XXX",
                "translations" : null,
                "examples" : null,
                "sound" : "sss"
              } ],
              "answered" : 42,
              "stats" : {
                "OPTIONS" : 42
              },
              "details" : {
                "a" : 42,
                "b" : [ "a", 42 ],
                "c" : "2006-02-13T00:00:00Z"
              },
              "changed-at" : "2013-12-13T00:00:00Z"
            }
        """.trimIndent()
        val exp = jacksonMapper.readTree(expected)
        val act = jacksonMapper.readTree(actual)
        Assertions.assertEquals(exp, act)
    }
}