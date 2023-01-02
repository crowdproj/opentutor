package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbExample
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbWord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class EntityMapperTest {

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

        private val testMemDbCard = MemDbCard(
            text = "snowfall",
            details = emptyMap(),
            words = listOf(
                MemDbWord(
                    word = "snowfall",
                    transcription = "ˈsnəʊfɔːl",
                    partOfSpeech = "noun",
                    translations = listOf(listOf("снегопад")),
                    examples = listOf(
                        MemDbExample(
                            translation = "Из-за сильного снегопада все рейсы отменены...",
                            text = "Due to the heavy snowfall, all flights have been cancelled...",
                        ),
                        MemDbExample(text = "It's the first snowfall of Christmas.")
                    )
                )
            ),
            answered = 42,
        )
    }

    @Test
    fun `test map document-card to mem-db-card`() {
        val givenCard = testDocumentCard.copy(status = DocumentCardStatus.LEARNED)
        val actualCard = givenCard.toMemDbCard {
            if (it == givenCard.status) testMemDbCard.answered!! else throw AssertionError()
        }
        Assertions.assertEquals(testMemDbCard, actualCard)
    }

    @Test
    fun `test map mem-db-card to document-card`() {
        val givenCard = testMemDbCard.copy(id = 1, dictionaryId = 2, answered = 42, details = mapOf("a" to "b"))
        val actualCard = givenCard.toDocumentCard {
            if (it == givenCard.answered) testDocumentCard.status else throw AssertionError()
        }
        Assertions.assertEquals(testDocumentCard, actualCard)
    }

}