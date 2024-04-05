package com.gitlab.sszuev.flashcards.common.documents

import com.gitlab.sszuev.flashcards.core.documents.DocumentCard
import com.gitlab.sszuev.flashcards.core.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.core.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.core.documents.DocumentReader
import com.gitlab.sszuev.flashcards.core.documents.DocumentWriter
import com.gitlab.sszuev.flashcards.core.documents.xml.LingvoDocumentReader
import com.gitlab.sszuev.flashcards.core.documents.xml.LingvoDocumentWriter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

internal class LingvoDocumentTest {
    companion object {

        private fun normalize(s: String): String {
            return s.replace("[\n\r\t]".toRegex(), "")
        }

        private fun createParser(): DocumentReader {
            return LingvoDocumentReader()
        }

        private fun createWriter(): DocumentWriter {
            return LingvoDocumentWriter()
        }

        @Suppress("SameParameterValue")
        private fun readDataAsString(resource: String): String {
            return LingvoDocumentTest::class.java.getResourceAsStream(resource)!!
                .bufferedReader(StandardCharsets.UTF_16)
                .use { it.readText() }
        }

        private fun readResourceDictionary(name: String): DocumentDictionary {
            return LingvoDocumentTest::class.java.getResourceAsStream(name)!!.use { createParser().parse(it) }
        }

        private fun assertDictionary(expected: DocumentDictionary, actual: DocumentDictionary) {
            Assertions.assertEquals(expected.name, actual.name)
            Assertions.assertEquals(expected.cards.size, actual.cards.size)
            Assertions.assertEquals(expected.targetLang, actual.targetLang)
            Assertions.assertEquals(expected.sourceLang, actual.sourceLang)
            val expectedCards = expected.cards
            val actualCards = actual.cards
            Assertions.assertEquals(expectedCards.size, actualCards.size)
            expectedCards.forEachIndexed { i, expectedCard ->
                val actualCard = actualCards[i]
                Assertions.assertEquals(expectedCard.text, actualCard.text)
                Assertions.assertEquals(expectedCard.transcription, actualCard.transcription)
                Assertions.assertEquals(expectedCard.partOfSpeech, actualCard.partOfSpeech)
                val origTranslations = expectedCard.translations.toList()
                val actualTranslations = actualCard.translations.toList()
                Assertions.assertEquals(origTranslations, actualTranslations)
                val expectedExamples = expectedCard.examples.toList()
                val actualExamples = actualCard.examples.toList()
                Assertions.assertEquals(expectedExamples, actualExamples)
            }
        }
    }

    @Test
    fun `test LingvoDictionaryWriter`() {
        val expected = normalize(readDataAsString("/documents/TestDictionaryEnRu.xml"))
        val card1 = DocumentCard(
            text = "rain",
            transcription = "rein",
            partOfSpeech = "noun",
            translations = listOf("дождь", "ливень"),
            examples = listOf("The skies no longer rain death.", "The sockets were filled with rain."),
            status = DocumentCardStatus.IN_PROCESS,
        )
        val card2 = DocumentCard(
            text = "mutual",
            transcription = "ˈmjuːʧʊəl",
            partOfSpeech = "adjective",
            translations = listOf("взаимный", "обоюдный", "общий", "совместный"),
            examples = listOf("Twenty years of mutual vanity, and nothing more."),
            status = DocumentCardStatus.LEARNED,
        )
        val card3 = DocumentCard(
            text = "test",
            transcription = "test",
            partOfSpeech = "verb",
            translations = listOf("тестировать"),
            status = DocumentCardStatus.UNKNOWN,
        )

        val dictionary = DocumentDictionary(
            name = "The Test Dictionary",
            sourceLang = "en",
            targetLang = "ru",
            cards = listOf(card1, card2, card3),
        )
        val out = ByteArrayOutputStream()
        createWriter().write(dictionary, out)
        val txt = out.toString(StandardCharsets.UTF_16)
        val actual = normalize(txt)
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `test LingvoDictionaryReader`() {
        // word + translations must be unique
        val cards: MutableSet<String> = mutableSetOf()
        val dic = readResourceDictionary("/documents/IrregularVerbsEnRu.xml")
        dic.cards.forEach { card ->
            val textWithTranslations = "${card.text} => ${card.translations.sorted().joinToString(", ")}"
            Assertions.assertTrue(cards.add(textWithTranslations))
            Assertions.assertFalse(textWithTranslations.contains("\n"))
        }
        Assertions.assertEquals(244, dic.cards.size)
    }

    @Test
    fun `test read-write round trip`(@TempDir dir: Path) {
        val orig = readResourceDictionary("/documents/WeatherEnRu.xml")
        val tmp = dir.resolve("test-WeatherEnRu.xml")

        Files.newOutputStream(tmp).use { createWriter().write(orig, it) }
        val reload1 = Files.newInputStream(tmp).use { createParser().parse(it) }
        assertDictionary(orig, reload1)

        Files.newOutputStream(tmp).use { createWriter().write(reload1, it) }
        val reload2 = Files.newInputStream(tmp).use { createParser().parse(it) }
        assertDictionary(orig, reload2)

        Files.newOutputStream(tmp).use { createWriter().write(reload2, it) }
        val reload3 = Files.newInputStream(tmp).use { createParser().parse(it) }
        assertDictionary(orig, reload3)

        assertDictionary(reload2, reload3)
        assertDictionary(reload1, reload2)
    }
}