package com.gitlab.sszuev.flashcards.dbmem.documents

import com.gitlab.sszuev.flashcards.dbmem.MemDbConfig
import com.gitlab.sszuev.flashcards.dbmem.dao.*
import com.gitlab.sszuev.flashcards.dbmem.documents.impl.LingvoDictionaryReader
import com.gitlab.sszuev.flashcards.dbmem.documents.impl.LingvoDictionaryWriter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

internal class LingvoDocumentTest {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LingvoDocumentTest::class.java)

        private val parser: DictionaryReader = LingvoDictionaryReader()
        private val writer: DictionaryWriter = LingvoDictionaryWriter(MemDbConfig(numberOfRightAnswers = 5))

        private fun normalize(s: String): String {
            return s.replace("[\n\r\t]".toRegex(), "")
        }

        @Suppress("SameParameterValue")
        private fun readDataAsString(resource: String): String {
            return LingvoDocumentTest::class.java.getResourceAsStream(resource)!!
                .bufferedReader(StandardCharsets.UTF_16)
                .use { it.readText() }
        }

        private fun toKey(c: Card): String {
            return "${c.text} => ${c.translations.map { it.text }.sorted().joinToString(", ")}"
        }

        private fun readResourceDictionary(name: String): Dictionary {
            return LingvoDocumentTest::class.java.getResourceAsStream(name)!!.use { parser.parse(it) }
        }

        private fun assertDictionary(expected: Dictionary, actual: Dictionary) {
            Assertions.assertEquals(expected.name, actual.name)
            Assertions.assertEquals(expected.cards.size, actual.cards.size)
            Assertions.assertEquals(expected.targetLanguage.id, actual.targetLanguage.id)
            Assertions.assertEquals(expected.sourceLanguage.id, actual.sourceLanguage.id)
            for (i in expected.cards.indices) {
                val expectedCard = expected.cards[i]
                val actualCard = actual.cards[i]
                Assertions.assertEquals(expectedCard.text, actualCard.text)
                Assertions.assertEquals(expectedCard.transcription, actualCard.transcription)
                Assertions.assertEquals(expectedCard.details, actualCard.details)
                Assertions.assertEquals(expectedCard.partOfSpeech, actualCard.partOfSpeech)
                Assertions.assertEquals(expectedCard.answered, actualCard.answered)
                val origTranslations = expectedCard.translations.toList()
                val actualTranslations = actualCard.translations.toList()
                Assertions.assertEquals(origTranslations.size, actualTranslations.size)
                val expectedExamples = expectedCard.examples.toList()
                val actualExamples = actualCard.examples.toList()
                Assertions.assertEquals(expectedExamples.size, actualExamples.size)
                for (j in origTranslations.indices) {
                    val expectedTranslation = origTranslations[j]
                    val actualTranslation = actualTranslations[j]
                    Assertions.assertEquals(expectedTranslation.text, actualTranslation.text)
                }
                for (j in expectedExamples.indices) {
                    val expectedExample = expectedExamples[j]
                    val actualExample = actualExamples[j]
                    Assertions.assertEquals(expectedExample.text, actualExample.text)
                }
            }
        }
    }

    @Test
    fun `test LingvoDictionaryWriter`() {
        val expected = normalize(readDataAsString("/TestDictionaryEnRu.xml"))
        val card1 = Card(
            id = 1,
            dictionaryId = 42,
            text = "rain",
            transcription = "rein",
            partOfSpeech = "noun",
            translations = listOf(
                Translation(id = 1, cardId = 1, text = "дождь"),
                Translation(id = 2, cardId = 1, text = "ливень")
            ),
            examples = listOf(
                Example(id = 1, cardId = 1, text = "The skies no longer rain death."),
                Example(id = 2, cardId = 1, text = "The sockets were filled with rain.")
            ),
            answered = 2,
        )
        val card2 = Card(
            id = 2,
            dictionaryId = 42,
            text = "mutual",
            transcription = "ˈmjuːʧʊəl",
            partOfSpeech = "adjective",
            translations = listOf(
                Translation(id = 3, cardId = 2, text = "взаимный"),
                Translation(id = 4, cardId = 2, text = "обоюдный"),
                Translation(id = 5, cardId = 2, text = "общий"),
                Translation(id = 6, cardId = 2, text = "совместный"),
            ),
            examples = listOf(
                Example(id = 3, cardId = 2, text = "Twenty years of mutual vanity, and nothing more."),
            ),
            answered = 12,
        )
        val card3 = Card(
            id = 3,
            dictionaryId = 42,
            text = "test",
            transcription = "test",
            partOfSpeech = "verb",
            translations = listOf(
                Translation(id = 7, cardId = 2, text = "тестировать"),
            ),
        )

        val dictionary = Dictionary(
            id = 42L,
            name = "The Test Dictionary",
            sourceLanguage = Language("en", "xxx"),
            targetLanguage = Language("ru", "xxx"),
            cards = listOf(card1, card2, card3)
        )
        val out = ByteArrayOutputStream()
        writer.write(dictionary, out)
        val txt = out.toString(StandardCharsets.UTF_16)
        LOGGER.info("\n{}", txt)
        val actual = normalize(txt)
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `test LingvoDictionaryReader`() {
        // word + translations must be unique
        val cards: MutableSet<String> = HashSet()
        val dic = readResourceDictionary("/data/BusinessEnRu.xml")
        dic.cards.forEach { c ->
            val w = toKey(c)
            LOGGER.info("{}", w)
            Assertions.assertTrue(cards.add(w))
            Assertions.assertFalse(w.contains("\n"))
        }
        Assertions.assertEquals(242, dic.cards.size)
    }

    @Test
    fun `test read-write round trip`(@TempDir dir: Path) {
        val orig = readResourceDictionary("/data/WeatherEnRu.xml")
        val tmp = dir.resolve("test-WeatherEnRu.xml")
        Files.newOutputStream(tmp).use { writer.write(orig, it) }
        val reload = Files.newInputStream(tmp).use { parser.parse(it) }
        assertDictionary(orig, reload)
    }
}