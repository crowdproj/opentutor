package com.gitlab.sszuev.flashcards.logs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gitlab.sszuev.flashcards.logs.models.CardLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardWordExampleLogResource
import com.gitlab.sszuev.flashcards.logs.models.CardWordLogResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

internal class LogResourcesSerializationTest {

    companion object {
        private val jacksonMapper = ObjectMapper().registerModule(JavaTimeModule())

        private fun serialize(response: Any): String = jacksonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response)

        private fun String.normalize() = this.replace("\\s".toRegex(), "")
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
            changedAt = OffsetDateTime.MAX.truncatedTo(ChronoUnit.MINUTES),
            details = mapOf(
                "a" to 42L,
                "b" to listOf("a", 42),
                "c" to OffsetDateTime.MIN.truncatedTo(ChronoUnit.MINUTES).plusDays(42).toInstant(),
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
                "c" : -31557014132032800.000000000
              },
              "changed-at" : 31556889832845540.000000000
            }
        """.trimIndent()
        Assertions.assertEquals(expected.normalize(), actual.normalize())
    }
}