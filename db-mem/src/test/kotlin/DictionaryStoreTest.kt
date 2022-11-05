package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.testutils.classPathResourceDir
import com.gitlab.sszuev.flashcards.dbmem.testutils.copyClassPathDataToDir
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

@Order(1)
internal class DictionaryStoreTest {

    @Test
    fun `test load dictionaries from class-path`() {
        val config = MemDbConfig().copy(dataLocation = "classpath:$classPathResourceDir")
        val dictionaries = DictionaryStore.load(dbConfig = config, ids = IdSequences())
        Assertions.assertEquals(2, dictionaries.size)
        Assertions.assertEquals("Business vocabulary (Job)", dictionaries[1]!!.name)
        Assertions.assertEquals("Weather", dictionaries[2]!!.name)

        Assertions.assertEquals(242, dictionaries[1]!!.cards.size)
        Assertions.assertEquals(65, dictionaries[2]!!.cards.size)
    }

    @Test
    fun `test load dictionaries from directory & flush & reload`(@TempDir dir: Path) {
        copyClassPathDataToDir(classPathResourceDir, dir)
        val dictionaries1 =
            DictionaryStore.load(dbConfig = MemDbConfig().copy(dataLocation = dir.toString()), ids = IdSequences())
        Assertions.assertEquals(2, dictionaries1.size)
        Assertions.assertEquals("Business vocabulary (Job)", dictionaries1[1]!!.name)
        Assertions.assertEquals("Weather", dictionaries1[2]!!.name)

        Assertions.assertEquals(242, dictionaries1[1]!!.cards.size)
        Assertions.assertEquals(65, dictionaries1[2]!!.cards.size)

        val card = MemDbCard(
            id = -42,
            dictionaryId = 2,
            text = "word",
            translations = listOf("слово")
        )
        dictionaries1[2]!!.cards[-42] = card
        dictionaries1.flush(2)

        val dictionaries2 =
            DictionaryStore.load(dbConfig = MemDbConfig().copy(dataLocation = dir.toString()), ids = IdSequences())
        Assertions.assertSame(dictionaries1, dictionaries2)
        Assertions.assertEquals(2, dictionaries2.size)
        Assertions.assertEquals(242, dictionaries2[1]!!.cards.size)
        Assertions.assertEquals(66, dictionaries2[2]!!.cards.size)
        val actual2 = dictionaries2[2]!!.cards.values.first { it.text == card.text }
        Assertions.assertNotNull(actual2)
        Assertions.assertSame(card, actual2)

        // wait 1 second (default period is 500 ms) and reload store
        Thread.sleep(1000)

        // clear to avoid caching
        DictionaryStore.clear()

        val dictionaries3 =
            DictionaryStore.load(dbConfig = MemDbConfig().copy(dataLocation = dir.toString()), ids = IdSequences())
        Assertions.assertNotSame(dictionaries1, dictionaries3)
        Assertions.assertEquals(2, dictionaries3.size)
        Assertions.assertEquals(242, dictionaries3[1]!!.cards.size)
        Assertions.assertEquals(66, dictionaries3[2]!!.cards.size)

        val actual3 = dictionaries3[2]!!.cards.values.first { it.text == card.text }
        Assertions.assertNotNull(actual3)
        Assertions.assertNotSame(actual2, actual3)
        Assertions.assertEquals(card.transcription, actual3.transcription)
        Assertions.assertEquals(card.translations, actual3.translations)
        Assertions.assertEquals(card.examples, actual3.examples)
    }

}
