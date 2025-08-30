package com.gitlab.sszuev.flashcards.core.mappers

import com.gitlab.sszuev.flashcards.AppConfig
import com.gitlab.sszuev.flashcards.core.documents.DocumentCard
import com.gitlab.sszuev.flashcards.core.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.model.domain.CardWordExampleEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DocMappersTest {
    @OptIn(ExperimentalTime::class)
    companion object {
        private val testDocumentCard = DocumentCard(
            text = "snowfall",
            transcription = "ˈsnəʊfɔːl",
            partOfSpeech = "noun",
            translations = listOf("снегопад"),
            examples = listOf(
                "Due to the heavy snowfall, all flights have been cancelled... -- Из-за сильного снегопада все рейсы отменены...",
                "It's the first snowfall of Christmas.",
            ),
            status = DocumentCardStatus.LEARNED,
        )

        private val testCardEntity = CardEntity(
            details = emptyMap(),
            words = listOf(
                CardWordEntity(
                    word = "snowfall",
                    transcription = "ˈsnəʊfɔːl",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("снегопад")),
                    examples = listOf(
                        CardWordExampleEntity(
                            translation = "Из-за сильного снегопада все рейсы отменены...",
                            text = "Due to the heavy snowfall, all flights have been cancelled...",
                        ),
                        CardWordExampleEntity(text = "It's the first snowfall of Christmas.")
                    )
                )
            ),
            answered = 15,
        )

        private fun assertSplitWords(expectedSize: Int, givenString: String) {
            val actual1: List<String> =
                fromDocumentCardTranslationToCommonWordDtoTranslation(givenString)
            Assertions.assertEquals(expectedSize, actual1.size)
            actual1.forEach { assertPhrasePart(it) }
            Assertions.assertEquals(
                expectedSize, fromDocumentCardTranslationToCommonWordDtoTranslation(
                    givenString
                ).size
            )
            val actual2: List<String> =
                fromDocumentCardTranslationToCommonWordDtoTranslation(givenString)
            Assertions.assertEquals(actual1, actual2)
        }

        private fun assertPhrasePart(s: String) {
            Assertions.assertFalse(s.isEmpty())
            Assertions.assertFalse(s.startsWith(" "))
            Assertions.assertFalse(s.endsWith(" "))
            if (!s.contains("(") || !s.contains(")")) {
                Assertions.assertFalse(s.contains(","), "For string '$s'")
            }
        }
    }

    @Test
    fun testSplitIntoWords() {
        assertSplitWords(0, " ")
        assertSplitWords(1, "a.  bb.xxx;yyy")
        assertSplitWords(6, "a,  ew,ewere;errt,&oipuoirwe,ор43ыфю,,,q,,")
        assertSplitWords(10, "mmmmmmmm, uuuuuu, uuu (sss, xzxx, aaa), ddd, sss, q, www,ooo , ppp, sss. in zzzzz")
        assertSplitWords(3, "s s s s (smth l.), d (smth=d., smth=g) x, (&,?)x y")
    }

    @Test
    fun `test map document-card to mem-db-card`() {
        val givenCard = testDocumentCard.copy(status = DocumentCardStatus.LEARNED)
        val actualCard = givenCard.toCardEntity(AppConfig.DEFAULT)
        Assertions.assertEquals(testCardEntity, actualCard)
    }

    @Test
    fun `test map mem-db-card to document-card`() {
        val givenCard = testCardEntity.copy(
            cardId = CardId("1"),
            dictionaryId = DictionaryId("2"),
            answered = 42,
            details = mapOf("a" to "b"),
        )
        val actualCard = givenCard.toXmlDocumentCard(AppConfig.DEFAULT)
        Assertions.assertEquals(testDocumentCard, actualCard)
    }
}